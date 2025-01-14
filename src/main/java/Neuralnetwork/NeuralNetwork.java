import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;

public class NeuralNetwork {
    private MultiLayerNetwork network;

    public NeuralNetwork() {
        // Define the CNN architecture
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                // First convolutional layer
                .layer(0, new ConvolutionLayer.Builder()
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .nIn(3) // 3 input channels
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                // Second convolutional layer
                .layer(1, new ConvolutionLayer.Builder()
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                // Dense layer
                .layer(2, new DenseLayer.Builder()
                        .nOut(256)
                        .activation(Activation.RELU)
                        .build())
                // Output layer
                .layer(3, new OutputLayer.Builder()
                        .nOut(1)
                        .activation(Activation.TANH) // Output between -1 and 1
                        .build())
                .setInputType(InputType.convolutional(7, 7, 3))
                .build();

        network = new MultiLayerNetwork(conf);
        network.init();
    }

    /**
     * Evaluates a board position, returning a value between -1 and 1.
     */
    public double evaluatePosition(Board board, Player currentPlayer) {
        INDArray input = BoardConverter.boardToTensor(board, currentPlayer);
        return network.output(input).getDouble(0);
    }
}
