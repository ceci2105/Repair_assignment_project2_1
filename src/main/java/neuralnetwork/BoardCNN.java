package neuralnetwork;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class BoardCNN {
    private MultiLayerNetwork model;

    public BoardCNN() {
        // Configure the CNN
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                // Input: 3 channels (player pieces, opponent pieces, empty) x 24 positions
                .layer(0, new ConvolutionLayer.Builder()
                        .nIn(3)
                        .nOut(32)
                        .kernelSize(1, 3) // Kernel over 3 positions
                        .stride(1, 1)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new BatchNormalization.Builder().build())
                .layer(2, new ConvolutionLayer.Builder()
                        .nOut(64)
                        .kernelSize(1, 3)
                        .stride(1, 1)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new BatchNormalization.Builder().build())
                .layer(4, new DenseLayer.Builder()
                        .nOut(128)
                        .activation(Activation.RELU)
                        .build())
                .layer(5, new OutputLayer.Builder()
                        .nOut(1)
                        .activation(Activation.SIGMOID)
                        .lossFunction(LossFunctions.LossFunction.XENT) // Use binary cross-entropy loss
                        .build())
                .setInputType(InputType.convolutionalFlat(1, 24, 3)) // Reshape board to 1x24 grid
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
    }

    public float evaluatePosition(float[] boardInput) {
        // Reshape input for CNN
        INDArray input = Nd4j.create(1, 3, 1, 24);  // batch size 1, 3 channels, 1x24 grid
        for (int i = 0; i < boardInput.length; i++) {
            int channel = i / 24;
            int pos = i % 24;
            input.putScalar(new int[]{0, channel, 0, pos}, boardInput[i]);
        }

        // Get model prediction
        INDArray output = model.output(input);
        return output.getFloat(0);
    }

    public void train(INDArray features, INDArray labels) {
        DataSet dataSet = new DataSet(features, labels);
        model.fit(dataSet);
    }
}