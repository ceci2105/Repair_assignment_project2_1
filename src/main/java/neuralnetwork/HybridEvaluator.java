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
    private BoardCNN cnn;
    private EvaluationFunction minimaxEval;
    private GameDataCollector dataCollector;

    public HybridEvaluator(Game game) {
        this.cnn = new BoardCNN();
        this.minimaxEval = new EvaluationFunction(game);
        this.dataCollector = new GameDataCollector();
    }

    public int evaluate(Board board, Player player, int phase, Node node) {
        if (phase == 1) {
            // Use CNN for placement phase
            float[] boardInput = dataCollector.boardToNNInput(board);
            float cnnScore = cnn.evaluatePosition(boardInput);
            return (int)(cnnScore * 1000); // Scale to similar range as minimax eval
        } else {
            // Use minimax evaluation for movement phases
            return minimaxEval.evaluate(board, player, phase, node);
        }
    }

    // Method to collect training data
    public void collectTrainingData(int numGames) {
        dataCollector.generateTrainingData(numGames);
        // Train CNN with collected data
        INDArray features = dataCollector.getTrainingFeatures();
        INDArray labels = dataCollector.getTrainingLabels();

        cnn.train(features, labels);
    }
}