package NeuralNetwork;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;

public class Trainer {

    public static void train(MultiLayerNetwork model, INDArray inputs, INDArray labels, int epochs) {
        // Attach a listener to monitor progress
        model.setListeners(new ScoreIterationListener(10));

        for (int epoch = 0; epoch < epochs; epoch++) {
            model.fit(inputs, labels); // Train the model
            System.out.println("Epoch " + epoch + " complete.");
        }
    }
}
