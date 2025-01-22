package neuralnetwork;

import game.mills.Game;
import game.mills.Board;
import game.mills.Player;
import javafx.scene.paint.Color;
import minimax.MinimaxAIPlayer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class GameDataCollector {
    private static final Logger log = Logger.getLogger(GameDataCollector.class.getName());
    private static final int NUM_WORKER_THREADS = 4;
    private static final int EARLY_GAME_THRESHOLD = 10;
    private static final float POSITION_SAMPLING_RATE = 0.1f;

    private final ExecutorService executorService;
    private final List<GameRecord> gameRecords;
    private final Random random;
    private final Board board;
    private Consumer<Integer> progressCallback;
    private final AtomicInteger gamesCompleted = new AtomicInteger(0);

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
        Game game = new Game(null, null);
        MinimaxAIPlayer minimaxPlayer = new MinimaxAIPlayer("Minimax", Color.WHITE, minimaxDepth, game);
        MinimaxAIPlayer opponent = new MinimaxAIPlayer("Opponent", Color.BLACK, minimaxDepth - 1, game);

        if (random.nextBoolean()) {
            game.setHumanPlayer1(minimaxPlayer);
            game.setSecondPlayer(opponent);
        } else {
            game.setHumanPlayer1(opponent);
            game.setSecondPlayer(minimaxPlayer);
        }

        GameRecord gameRecord = new GameRecord();
        int moveCount = 0;

        while (!game.isGameOver()) {
            if (shouldRecordPosition(moveCount)) {
                BoardPosition position = recordCurrentPosition(game);
                gameRecord.addPosition(position);
            }

            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer instanceof MinimaxAIPlayer) {
                ((MinimaxAIPlayer) currentPlayer).makeMove(board, game.getPhase());
            }

            moveCount++;
        }

        Player winner = game.getWinner();
        float gameOutcome = calculateGameOutcome(winner, minimaxPlayer);
        gameRecord.setOutcome(gameOutcome);
        gameRecord.finalizePositions();

        synchronized (gameRecords) {
            gameRecords.add(gameRecord);
            updateProgress();
        }
    }

    private boolean shouldRecordPosition(int moveCount) {
        return moveCount >= EARLY_GAME_THRESHOLD && random.nextFloat() < POSITION_SAMPLING_RATE;
    }

    private BoardPosition recordCurrentPosition(Game game) {
        Board board = game.getBoard();
        Player currentPlayer = game.getCurrentPlayer();
        float[][][] boardTensor = BoardStateConverter.convertToTensor(board, currentPlayer);
        return new BoardPosition(boardTensor, 0.0f);
    }

    private float calculateGameOutcome(Player winner, Player minimaxPlayer) {
        if (winner == null) {
            return 0.0f;
        }
        return winner == minimaxPlayer ? 1.0f : -1.0f;
    }

    public INDArray getTrainingFeatures() throws IOException {
        File[] gameFiles = getGameRecordFiles();
        List<INDArray> featureBatches = new ArrayList<>();

        for (File file : gameFiles) {
            GameRecord record = loadGameRecordFromFile(file);

            INDArray batchFeatures = Nd4j.create(record.getPositions().size(), 4, 7, 7);
            int index = 0;

            for (BoardPosition position : record.getPositions()) {
                float[][][] tensorData = position.getBoardState();
                for (int c = 0; c < 4; c++) {
                    for (int h = 0; h < 7; h++) {
                        for (int w = 0; w < 7; w++) {
                            batchFeatures.putScalar(new int[]{index, c, h, w}, tensorData[c][h][w]);
                        }
                    }
                }
                index++;
            }
            featureBatches.add(batchFeatures);
        }

        return Nd4j.vstack(featureBatches);
    }

    private File[] getGameRecordFiles() {
        File directory = new File("path/to/game/records");
        return directory.listFiles((dir, name) -> name.endsWith(".dat"));
    }

    private GameRecord loadGameRecordFromFile(File file) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameRecord) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Failed to deserialize GameRecord", e);
        }
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