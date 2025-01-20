package neural;

import game.mills.*;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import minimax.MinimaxAIPlayer;
import agents.neural_network.BaselineAgent;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.java.Log;

/**
 * Handles the generation and collection of game data by simulating games
 * between
 * Minimax and Baseline agents. Implements intelligent filtering of positions to
 * ensure quality training data.
 */
@Log
public class GameDataCollector {
    private static final int EARLY_GAME_THRESHOLD = 6; // Number of moves considered "early game"
    private static final float POSITION_SAMPLING_RATE = 0.7f; // Probability of recording a position after early game
    private static final int NUM_WORKER_THREADS = Runtime.getRuntime().availableProcessors();

    private final ExecutorService executorService;
    private final List<GameRecord> gameRecords;
    private final Random random;
    private Consumer<Integer> progressCallback;
    private AtomicInteger gamesCompleted = new AtomicInteger(0);

    public GameDataCollector() {
        this.executorService = Executors.newFixedThreadPool(NUM_WORKER_THREADS);
        this.gameRecords = Collections.synchronizedList(new ArrayList<>());
        this.random = new Random();
    }

    public void setProgressCallback(Consumer<Integer> callback) {
        this.progressCallback = callback;
    }

    private void updateProgress() {
        if (progressCallback != null) {
            progressCallback.accept(gamesCompleted.incrementAndGet());
        }
    }

    /**
     * Generates game data by playing multiple games between Minimax and Baseline
     * agents.
     * 
     * @param numGames     Number of games to simulate
     * @param minimaxDepth Search depth for the Minimax agent
     */
    public void generateGames(int numGames, int minimaxDepth) {
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < numGames; i++) {
            futures.add(executorService.submit(() -> playAndRecordGame(minimaxDepth)));
        }

        // Wait for all games to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.severe("Error generating game: " + e.getMessage());
            }
        }
    }

    /**
     * Plays a single game between Minimax and Baseline agents and records relevant
     * positions.
     */
    private void playAndRecordGame(int minimaxDepth) {
        // Create players
        BaselineAgent baselinePlayer = new BaselineAgent("Baseline", javafx.scene.paint.Color.RED);

        Game game = new Game(baselinePlayer, null); // Initialize game first
        MinimaxAIPlayer minimaxPlayer = new MinimaxAIPlayer("Minimax", javafx.scene.paint.Color.BLUE, minimaxDepth,
                game);

        // Randomly assign colors
        if (random.nextBoolean()) {
            // game.setPlayer1(minimaxPlayer);
            game.setSecondPlayer(baselinePlayer);
        } else {
            // game.setPlayer1(baselinePlayer);
            game.setSecondPlayer(minimaxPlayer);
        }

        GameRecord gameRecord = new GameRecord();
        int moveCount = 0;

        // Play the game
        while (!game.isGameOver()) {
            // Record the position if it meets our criteria
            if (shouldRecordPosition(moveCount)) {
                BoardPosition position = recordCurrentPosition(game);
                gameRecord.addPosition(position);
            }

            // Make move
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer instanceof MinimaxAIPlayer) {
                ((MinimaxAIPlayer) currentPlayer).makeMove(game.getBoard(), game.getPhase());
            } else {
                ((BaselineAgent) currentPlayer).makeMove();
            }

            moveCount++;
        }

        // Record final outcome
        Player winner = game.getWinner();
        float gameOutcome = calculateGameOutcome(winner, minimaxPlayer);
        gameRecord.setOutcome(gameOutcome);

        // Apply outcome to all positions
        gameRecord.finalizePositions();
        // After game completes
        synchronized (gameRecords) {
            gameRecords.add(gameRecord);
            updateProgress();
        }

        // Add to collection
        synchronized (gameRecords) {
            gameRecords.add(gameRecord);
        }
    }

    /**
     * Determines if the current position should be recorded based on game phase
     * and random sampling.
     */
    private boolean shouldRecordPosition(int moveCount) {
        if (moveCount < EARLY_GAME_THRESHOLD) {
            return false; // Skip early game positions
        }
        return random.nextFloat() < POSITION_SAMPLING_RATE;
    }

    /**
     * Records the current board position and creates a BoardPosition object.
     */
    private BoardPosition recordCurrentPosition(Game game) {
        Board board = game.getBoard();
        Player currentPlayer = game.getCurrentPlayer();

        // Convert board to tensor format
        float[][][] boardTensor = BoardStateConverter.convertToTensor(board, currentPlayer);

        return new BoardPosition(boardTensor, 0.0f); // Initial evaluation will be set later
    }

    /**
     * Calculates the game outcome from Minimax's perspective.
     * Returns 1.0 for win, -1.0 for loss, 0.0 for draw
     */
    private float calculateGameOutcome(Player winner, Player minimaxPlayer) {
        if (winner == null) {
            return 0.0f; // Draw
        }
        return winner == minimaxPlayer ? 1.0f : -1.0f;
    }

    /**
     * Saves the collected game data to a file.
     */
    public void saveGameData(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(gameRecords);
        }
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}

/**
 * Represents a single game's worth of positions and their final outcome.
 */
class GameRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<BoardPosition> positions;
    private float outcome;

    public GameRecord() {
        this.positions = new ArrayList<>();
    }

    public void addPosition(BoardPosition position) {
        positions.add(position);
    }

    public void setOutcome(float outcome) {
        this.outcome = outcome;
    }

    /**
     * Updates all positions with the game's outcome, with some decay
     * for positions further from the end of the game.
     */
    public void finalizePositions() {
        float discount = 0.95f; // Discount factor for earlier positions
        float currentValue = outcome;

        // Work backwards from the end of the game
        for (int i = positions.size() - 1; i >= 0; i--) {
            positions.get(i).setEvaluation(currentValue);
            currentValue *= discount; // Reduce the impact for earlier positions
        }
    }
}

/**
 * Represents a single board position and its evaluation.
 */
class BoardPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    private final float[][][] boardState;
    private float evaluation;

    public BoardPosition(float[][][] boardState, float evaluation) {
        this.boardState = boardState;
        this.evaluation = evaluation;
    }

    public void setEvaluation(float evaluation) {
        this.evaluation = evaluation;
    }

    public float[][][] getBoardState() {
        return boardState;
    }

    public float getEvaluation() {
        return evaluation;
    }
}
