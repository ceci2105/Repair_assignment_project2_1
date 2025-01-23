package NeuralNetwork;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.learning.config.Adam;

public class CNNModel {

    public static MultiLayerNetwork buildNetwork(int inputSize) {
        MultiLayerNetwork model = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(123) // Seed for reproducibility
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001)) // Adam optimizer
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(inputSize) // Input size
                        .nOut(64)       // Number of neurons in the first layer
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(64)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder()
                        .nIn(32)
                        .nOut(1) // Single output (evaluation score)
                        .activation(Activation.IDENTITY) // Linear output
                        .lossFunction(LossFunctions.LossFunction.MSE) // Mean squared error loss
                        .build())
                .build());

        model.init(); // Initialize the model
        return model;
    }
}
