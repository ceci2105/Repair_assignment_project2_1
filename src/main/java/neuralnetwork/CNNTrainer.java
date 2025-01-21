package neuralnetwork;

import game.mills.Game;
import game.mills.Board;
import game.mills.Node;
import game.mills.Player;
import minimax.EvaluationFunction;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.*;
import java.util.List;

public class CNNTrainer {
    private CNNModel cnn;
    private GameDataCollector collector;
    private Game game;
    private EvaluationFunction minimaxEval;

    public CNNTrainer(Game game) {
        this.game = game;
        this.cnn = new CNNModel();
        this.minimaxEval = new EvaluationFunction(game);
        this.collector = new GameDataCollector();

        collector.setProgressCallback(gamesCompleted ->
                System.out.println("Completed " + gamesCompleted + " games"));
    }

    // Train the CNN using generated game data
    public void trainCNN(int numGames, int minimaxDepth) {
        System.out.println("Starting training data generation...");

        // Generate training data
        collector.generateGames(numGames, minimaxDepth);

        System.out.println("Data generation complete. Starting CNN training...");

        // Retrieve features and labels for training
        INDArray features = collector.getTrainingFeatures();
        INDArray labels = collector.getTrainingLabels();

        // Train the CNN model
        cnn.train(features, labels);

        System.out.println("CNN training complete!");
    }

    // Evaluate a board position using the trained CNN or Minimax
    public int evaluate(Board board, Player player, int phase, Node node) {
        if (phase == 1) {
            // Use CNN for early-game evaluation
            float[][][] boardTensor = BoardStateConverter.convertToTensor(board, player);
            float cnnScore = cnn.evaluatePosition(boardTensor);
            return (int) (cnnScore * 1000); // Scale CNN output for comparison
        } else {
            // Use Minimax for later phases
            return minimaxEval.evaluate(board, player, phase, node);
        }
    }

    // Save the trained CNN model
    public void saveModel(String filepath) {
        try {
            ModelSerializer.writeModel(cnn.getModel(), new File(filepath), true);
            System.out.println("Model saved successfully at: " + filepath);
        } catch (IOException e) {
            System.err.println("Failed to save model: " + e.getMessage());
        }
    }

    // Load a previously saved CNN model
    public void loadModel(String filepath) {
        try {
            cnn.setModel(ModelSerializer.restoreMultiLayerNetwork(new File(filepath)));
            System.out.println("Model loaded successfully from: " + filepath);
        } catch (IOException e) {
            System.err.println("Failed to load model: " + e.getMessage());
        }
    }

    // Save the training data (game records)
    public void saveGameData(String filepath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(collector.getGameRecords());
            System.out.println("Training data saved successfully at: " + filepath);
        } catch (IOException e) {
            System.err.println("Failed to save game data: " + e.getMessage());
        }
    }

    // Load the training data (game records)
    @SuppressWarnings("unchecked")
    public void loadGameData(String filepath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            List<GameRecord> gameRecords = (List<GameRecord>) ois.readObject();
            collector.setGameRecords(gameRecords);
            System.out.println("Training data loaded successfully from: " + filepath);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game data: " + e.getMessage());
        }
    }

    // Main method for demonstration
    public static void main(String[] args) {
        // Example usage
        Game game = new Game(null, null); // Create your game instance
        CNNTrainer trainer = new CNNTrainer(game);

        // Step 1: Train the CNN
        trainer.trainCNN(1000, 3); // Train with 1000 games, depth 3

        // Step 2: Save the trained CNN model and training data
        trainer.saveModel("cnnModel.zip");
        trainer.saveGameData("trainingData.dat");

        // Step 3: Load the model and data for evaluation
        trainer.loadModel("cnnModel.zip");
        trainer.loadGameData("trainingData.dat");

        // Step 4: Test the trained CNN
        Board currentBoard = game.getBoard();
        Player currentPlayer = game.getCurrentPlayer();
        float evaluation = trainer.evaluate(currentBoard, currentPlayer, 1, null);

        System.out.println("Position evaluation: " + evaluation);
    }
}
