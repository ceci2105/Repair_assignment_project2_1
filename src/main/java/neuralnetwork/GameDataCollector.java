package neuralnetwork;

import game.mills.*;
import minimax.MinimaxAIPlayer;
import javafx.scene.paint.Color;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import java.util.*;
import java.util.concurrent.*;

public class GameDataCollector {
    private static final int BATCH_SIZE = 32;
    private static final int MAX_POSITIONS_PER_GAME = 200;
    private static final int NUM_WORKER_THREADS = 2;

    private final ExecutorService executorService;
    private final Queue<BoardPosition> positionBuffer;
    private final Random random;
    private int gamesCompleted = 0;

    public GameDataCollector() {
        this.executorService = Executors.newFixedThreadPool(NUM_WORKER_THREADS);
        this.positionBuffer = new ConcurrentLinkedQueue<>();
        this.random = new Random();
    }

    public void generateGames(int numGames, int minimaxDepth, CNNModel cnn) {
        // Process games in batches to manage memory
        for (int batch = 0; batch < numGames; batch += BATCH_SIZE) {
            int batchSize = Math.min(BATCH_SIZE, numGames - batch);
            generateGameBatch(batchSize, minimaxDepth);
            trainOnCurrentBatch(cnn);
            positionBuffer.clear(); // Clear buffer after training
            System.gc(); // Suggest garbage collection between batches
        }
    }

    private void generateGameBatch(int batchSize, int minimaxDepth) {
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < batchSize; i++) {
            futures.add(executorService.submit(() -> playAndRecordGame(minimaxDepth)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                System.err.println("Error generating game: " + e.getMessage());
            }
        }
    }

    private void playAndRecordGame(int minimaxDepth) {
        Game game = new Game(null, null);
        MinimaxAIPlayer player1 = new MinimaxAIPlayer("P1", Color.WHITE, minimaxDepth, game);
        game.setHumanPlayer1(player1);
        game.setCurrentPlayer(player1);
        MinimaxAIPlayer player2 = new MinimaxAIPlayer("P2", Color.BLACK, minimaxDepth - 1, game);
        game.setSecondPlayer(player2);

        try {

            int positionsRecorded = 0;

            while (!game.isGameOver && positionsRecorded < MAX_POSITIONS_PER_GAME) {
                if (random.nextFloat() < 0.3f) {
                    recordPosition(game);
                    positionsRecorded++;
                }

                Player currentPlayer = game.getCurrentPlayer();
                if (currentPlayer instanceof MinimaxAIPlayer) {
                    ((MinimaxAIPlayer) currentPlayer).makeMove(game.getBoard(), game.getPhase());
                }
            }

            gamesCompleted++;
            if (gamesCompleted % 10 == 0) {
                System.out.println("Completed " + gamesCompleted + " games");
            }

        } catch (Exception e) {
            System.err.println("Error in game generation: " + e.getMessage());
        }
    }

    private void recordPosition(Game game) {
        float[][][] boardTensor = BoardStateConverter.convertToTensor(game.getBoard(), game.getCurrentPlayer());
        float evaluation = evaluatePosition(game);
        positionBuffer.offer(new BoardPosition(boardTensor, evaluation));
    }

    private float evaluatePosition(Game game) {
        Board board = game.getBoard();
        Player currentPlayer = game.getCurrentPlayer();
        int playerPieces = countPieces(board, currentPlayer);
        int opponentPieces = countPieces(board, game.getOpponent(currentPlayer));
        return (playerPieces - opponentPieces) / 9.0f; // Normalize to [-1, 1]
    }

    private int countPieces(Board board, Player player) {
        return (int) board.getNodes().values().stream()
                .filter(node -> node.getOccupant() == player)
                .count();
    }


    private void trainOnCurrentBatch(CNNModel cnn) {
        if (positionBuffer.isEmpty()) return;

        int batchSize = positionBuffer.size();
        INDArray features = Nd4j.create(batchSize, 4, 7, 7);
        INDArray labels = Nd4j.create(batchSize, 1);

        int index = 0;
        for (BoardPosition position : positionBuffer) {
            float[][][] tensorData = position.getBoardState();
            for (int c = 0; c < 4; c++) {
                for (int h = 0; h < 7; h++) {
                    for (int w = 0; w < 7; w++) {
                        features.putScalar(new int[]{index, c, h, w}, tensorData[c][h][w]);
                    }
                }
            }
            labels.putScalar(index, position.getEvaluation());
            index++;
        }

        cnn.train(features, labels);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

class BoardPosition {
    private final float[][][] boardState;
    private final float evaluation;

    public BoardPosition(float[][][] boardState, float evaluation) {
        this.boardState = boardState;
        this.evaluation = evaluation;
    }

    public float[][][] getBoardState() {
        return boardState;
    }

    public float getEvaluation() {
        return evaluation;
    }
}