package neuralnetwork;

import game.mills.*;
import minimax.MinimaxAIPlayer;
import javafx.scene.paint.Color;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import lombok.extern.java.Log;
import game.mills.Board;

@Log
public class GameDataCollector {
    private static final int EARLY_GAME_THRESHOLD = 6;
    private static final float POSITION_SAMPLING_RATE = 0.7f;
    private static final int NUM_WORKER_THREADS = Runtime.getRuntime().availableProcessors();

    private final ExecutorService executorService;
    private final List<GameRecord> gameRecords;
    private final Random random;
    private Consumer<Integer> progressCallback;
    private AtomicInteger gamesCompleted = new AtomicInteger(0);
    private Board board ;

    public GameDataCollector() {
        this.executorService = Executors.newFixedThreadPool(NUM_WORKER_THREADS);
        this.gameRecords = Collections.synchronizedList(new ArrayList<>());
        this.random = new Random();
        this.board = new Board();
    }

    public void setProgressCallback(Consumer<Integer> callback) {
        this.progressCallback = callback;
    }

    private void updateProgress() {
        if (progressCallback != null) {
            progressCallback.accept(gamesCompleted.incrementAndGet());
        }
    }

    public void generateGames(int numGames, int minimaxDepth) {
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < numGames; i++) {
            futures.add(executorService.submit(() -> playAndRecordGame(minimaxDepth)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.severe("Error generating game: " + e.getMessage());
            }
        }
    }

    private void playAndRecordGame(int minimaxDepth) {
        // Create players and game
        Game game = new Game(null, null); // Initialize game first
        MinimaxAIPlayer minimaxPlayer = new MinimaxAIPlayer("Minimax", Color.WHITE, minimaxDepth, game);
        MinimaxAIPlayer opponent = new MinimaxAIPlayer("Opponent", Color.BLACK, minimaxDepth - 1, game);

        // Randomly assign colors
        if (random.nextBoolean()) {
            game.setHumanPlayer1(minimaxPlayer);
            game.setSecondPlayer(opponent);
        } else {
            game.setHumanPlayer1(opponent);
            game.setSecondPlayer(minimaxPlayer);
        }

        GameRecord gameRecord = new GameRecord();
        int moveCount = 0;

        while (!(game.isGameOver)) {
            // Record position if it meets criteria
            if (shouldRecordPosition(moveCount)) {
                BoardPosition position = recordCurrentPosition(game);
                gameRecord.addPosition(position);
            }

            // Make move
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer instanceof MinimaxAIPlayer) {
                ((MinimaxAIPlayer) currentPlayer).makeMove(board, game.getPhase());
            }

            moveCount++;
        }

        // Record final outcome
        Player winner = game.getWinner();
        float gameOutcome = calculateGameOutcome(winner, minimaxPlayer);
        gameRecord.setOutcome(gameOutcome);

        // Apply outcome to all positions
        gameRecord.finalizePositions();

        synchronized (gameRecords) {
            gameRecords.add(gameRecord);
            updateProgress();
        }
    }

    private boolean shouldRecordPosition(int moveCount) {
        if (moveCount < EARLY_GAME_THRESHOLD) {
            return false; // Skip early game positions
        }
        return random.nextFloat() < POSITION_SAMPLING_RATE;
    }

    private BoardPosition recordCurrentPosition(Game game) {
        Board board = game.getBoard();
        Player currentPlayer = game.getCurrentPlayer();
        float[][][] boardTensor = BoardStateConverter.convertToTensor(board, currentPlayer);
        return new BoardPosition(boardTensor, 0.0f);
    }

    private float calculateGameOutcome(Player winner, Player minimaxPlayer) {
        if (winner == null) {
            return 0.0f; // Draw
        }
        return winner == minimaxPlayer ? 1.0f : -1.0f;
    }

    // Convert collected data to DL4J format
    public INDArray getTrainingFeatures() {
        int totalPositions = gameRecords.stream()
                .mapToInt(record -> record.getPositions().size())
                .sum();

        INDArray features = Nd4j.create(totalPositions, 4, 7, 7);
        int currentIndex = 0;

        for (GameRecord record : gameRecords) {
            for (BoardPosition position : record.getPositions()) {
                float[][][] tensorData = position.getBoardState();
                for (int c = 0; c < 4; c++) {
                    for (int h = 0; h < 7; h++) {
                        for (int w = 0; w < 7; w++) {
                            features.putScalar(new int[]{currentIndex, c, h, w}, tensorData[c][h][w]);
                        }
                    }
                }
                currentIndex++;
            }
        }
        return features;
    }

    public INDArray getTrainingLabels() {
        int totalPositions = gameRecords.stream()
                .mapToInt(record -> record.getPositions().size())
                .sum();

        INDArray labels = Nd4j.create(totalPositions, 1);
        int currentIndex = 0;

        for (GameRecord record : gameRecords) {
            for (BoardPosition position : record.getPositions()) {
                labels.putScalar(currentIndex++, position.getEvaluation());
            }
        }
        return labels;
    }

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

    public List<GameRecord> getGameRecords() {
        return gameRecords;
    }

    // Setter for game records
    public void setGameRecords(List<GameRecord> gameRecords) {
        synchronized (this.gameRecords) {
            this.gameRecords.clear();
            this.gameRecords.addAll(gameRecords);
        }
    }
}

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

    public List<BoardPosition> getPositions() {
        return positions;
    }

    public void finalizePositions() {
        float discount = 0.95f;
        float currentValue = outcome;

        for (int i = positions.size() - 1; i >= 0; i--) {
            positions.get(i).setEvaluation(currentValue);
            currentValue *= discount;
        }
    }
}

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