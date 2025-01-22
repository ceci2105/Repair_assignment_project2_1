package neuralnetwork;

import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
import minimax.EvaluationFunction;
import org.deeplearning4j.util.ModelSerializer;
import java.io.*;

public class CNNTrainer {
    private CNNModel cnn;
    private GameDataCollector collector;
    private Game game;
    private EvaluationFunction minimaxEval;
    private static final int EARLY_GAME_MOVES = 6; // Number of moves considered "early game"

    public CNNTrainer(Game game) {
        this.game = game;
        this.cnn = new CNNModel();
        this.minimaxEval = new EvaluationFunction(game);
        this.collector = new GameDataCollector();
    }

    // Train the CNN using generated game data with batched processing
    public void trainCNN(int numGames, int minimaxDepth) {
        System.out.println("Starting training with batched processing...");

        try {
            // The collector now handles both data generation and training
            collector.generateGames(numGames, minimaxDepth, cnn);

            System.out.println("Training complete!");

        } catch (Exception e) {
            System.err.println("Error during training: " + e.getMessage());
            e.printStackTrace();
        } finally {
            collector.shutdown();
        }
    }

    // Hybrid evaluation function combining CNN and Minimax
    public float evaluate(Board board, Player player, int moveCount, Node node) {
        try {
            if (moveCount < EARLY_GAME_MOVES) {
                // Use CNN for early game evaluation
                float[][][] boardTensor = BoardStateConverter.convertToTensor(board, player);
                return cnn.evaluatePosition(boardTensor);
            } else {
                // Use traditional evaluation for mid/late game
                return minimaxEval.evaluate(board, player, getCurrentPhase(moveCount), node) / 1000.0f;
            }
        } catch (Exception e) {
            System.err.println("Evaluation error: " + e.getMessage());
            // Fallback to minimax evaluation in case of error
            return minimaxEval.evaluate(board, player, getCurrentPhase(moveCount), node) / 1000.0f;
        }
    }

    // Determine game phase based on move count
    private int getCurrentPhase(int moveCount) {
        if (moveCount < 18) return 1; // Placement phase
        if (moveCount < 50) return 2; // Moving phase
        return 3; // Flying phase
    }

    // Save the trained model with error handling
    public void saveModel(String filepath) {
        try {
            File file = new File(filepath);
            file.getParentFile().mkdirs(); // Create directories if they don't exist
            ModelSerializer.writeModel(cnn.getModel(), file, true);
            System.out.println("Model saved successfully at: " + filepath);
        } catch (IOException e) {
            System.err.println("Failed to save model: " + e.getMessage());
        }
    }

    // Load a saved model with error handling
    public void loadModel(String filepath) {
        try {
            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException("Model file not found: " + filepath);
            }
            cnn.setModel(ModelSerializer.restoreMultiLayerNetwork(file));
            System.out.println("Model loaded successfully from: " + filepath);
        } catch (IOException e) {
            System.err.println("Failed to load model: " + e.getMessage());
        }
    }

    // Main method demonstrating usage with error handling
    public static void main(String[] args) {
        try {
            // Set up the game and trainer
            Game game = new Game(null, null);
            CNNTrainer trainer = new CNNTrainer(game);

            // Configure training parameters
            int numGames = 100;  // Reduced number of games for testing
            int minimaxDepth = 2;  // Reduced depth for faster processing

            // Train the model
            System.out.println("Starting training with " + numGames + " games...");
            trainer.trainCNN(numGames, minimaxDepth);

            // Save the trained model
            trainer.saveModel("models/cnn_model_" + System.currentTimeMillis() + ".zip");

            // Test the model
            Board testBoard = game.getBoard();
            Player testPlayer = game.getCurrentPlayer();
            float evaluation = trainer.evaluate(testBoard, testPlayer, 3, null);
            System.out.println("Test position evaluation: " + evaluation);

        } catch (Exception e) {
            System.err.println("Error in main: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters and setters
    public CNNModel getCnn() {
        return cnn;
    }

    public void setCnn(CNNModel cnn) {
        this.cnn = cnn;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}