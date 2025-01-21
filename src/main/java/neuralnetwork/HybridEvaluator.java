package neuralnetwork;

import game.mills.Board;
import game.mills.Game;
import game.mills.Player;
import game.mills.Node;
import lombok.extern.java.Log;
import minimax.EvaluationFunction;
import org.nd4j.linalg.api.ndarray.INDArray;

@Log
public class HybridEvaluator {
    private CNNModel cnn;  // You'll need to create this class for your CNN implementation
    private EvaluationFunction minimaxEval;
    private GameDataCollector dataCollector;

    public HybridEvaluator(Game game) {
        this.cnn = new CNNModel();  // Initialize your CNN model
        this.minimaxEval = new EvaluationFunction(game);
        this.dataCollector = new GameDataCollector();
    }

    public int evaluate(Board board, Player player, int phase, Node node) {
        if (phase == 1) {
            // Use CNN for placement phase
            float[][][] boardTensor = BoardStateConverter.convertToTensor(board, player);
            float cnnScore = cnn.evaluatePosition(boardTensor);
            return (int)(cnnScore * 1000); // Scale to similar range as minimax eval
        } else {
            // Use minimax evaluation for movement phases
            return minimaxEval.evaluate(board, player, phase, node);
        }
    }

    // Method to collect training data and train the CNN
    public void trainCNN(int numGames) {
        dataCollector.generateTrainingData(numGames);
        INDArray features = dataCollector.getTrainingFeatures();
        INDArray labels = dataCollector.getTrainingLabels();

        // Train the CNN model
        cnn.train(features, labels);
    }
}