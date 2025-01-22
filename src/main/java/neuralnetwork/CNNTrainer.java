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

    public void trainCNN(int numGames, int minimaxDepth) {
        System.out.println("Starting training data generation...");

        collector.generateGames(numGames, minimaxDepth);

        System.out.println("Data generation complete. Starting CNN training...");

        try {
            INDArray features = collector.getTrainingFeatures();
            INDArray labels = collector.getTrainingLabels();

            if (features.size(0) == 0 || labels.size(0) == 0) {
                throw new IllegalStateException("No training data generated.");
            }

            cnn.train(features, labels);

            System.out.println("CNN training complete!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int evaluate(Board board, Player player, int phase, Node node) {
        if (phase == 1) {
            float[][][] boardTensor = BoardStateConverter.convertToTensor(board, player);
            float cnnScore = cnn.evaluatePosition(boardTensor);
            return (int) (cnnScore * 1000);
        } else {
            return minimaxEval.evaluate(board, player, phase, node);
        }
    }

    public void saveModel(String filepath) {
        try {
            ModelSerializer.writeModel(cnn.getModel(), new File(filepath), true);
            System.out.println("Model saved successfully at: " + filepath);
        } catch (IOException e) {
            System.err.println("Failed to save model: " + e.getMessage());
        }
    }

    public void loadModel(String filepath) {
        try {
            cnn.setModel(ModelSerializer.restoreMultiLayerNetwork(new File(filepath)));
            System.out.println("Model loaded successfully from: " + filepath);
        } catch (IOException e) {
            System.err.println("Failed to load model: " + e.getMessage());
        }
    }

    public void saveGameData(String filepath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(collector.getGameRecords());
            System.out.println("Training data saved successfully at: " + filepath);
        } catch (IOException e) {
            System.err.println("Failed to save game data: " + e.getMessage());
        }
    }

    public void loadGameData(String filepath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            List<GameRecord> gameRecords = (List<GameRecord>) ois.readObject();
            collector.setGameRecords(gameRecords);
            System.out.println("Training data loaded successfully from: " + filepath);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Game game = new Game(null, null);
        CNNTrainer trainer = new CNNTrainer(game);

        trainer.trainCNN(50, 3);

        trainer.saveModel("cnnModel.zip");
        trainer.saveGameData("trainingData.dat");

        trainer.loadModel("cnnModel.zip");
        trainer.loadGameData("trainingData.dat");

        Board currentBoard = game.getBoard();
        Player currentPlayer = game.getCurrentPlayer();
        float evaluation = trainer.evaluate(currentBoard, currentPlayer, 1, null);

        System.out.println("Position evaluation: " + evaluation);
    }
}