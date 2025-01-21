package neuralnetwork;

import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class CNNModel {
    private MultiLayerNetwork model;

    public CNNModel() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                .layer(new ConvolutionLayer.Builder(3, 3)
                        .nIn(3) // Input channels
                        .stride(1, 1)
                        .nOut(64) // Output channels
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new ConvolutionLayer.Builder(3, 3)
                        .nOut(32) // Output channels
                        .stride(1, 1)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(1 * 1 * 32) // Flattened input from previous layer
                        .nOut(64)        // Number of neurons
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(64) // Number of inputs from the DenseLayer
                        .nOut(1) // Single output for regression
                        .activation(Activation.IDENTITY)
                        .build())
                .setInputType(InputType.convolutional(7, 7, 4)) // Input shape
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
    }

    public float evaluatePosition(float[][][] boardTensor) {
        // Reshape input to match network expectations [batch, channels, height, width]
        INDArray input = Nd4j.create(1, 4, 7, 7);
        for (int c = 0; c < 4; c++) {
            for (int h = 0; h < 7; h++) {
                for (int w = 0; w < 7; w++) {
                    input.putScalar(new int[]{0, c, h, w}, boardTensor[c][h][w]);
                }
            }
        }
        INDArray output = model.output(input);
        return output.getFloat(0);
    }

    public void train(INDArray features, INDArray labels) {
        // Ensure features are in the correct shape [batch, channels, height, width]
        if (features.rank() != 4) {
            features = features.reshape(features.size(0), 4, 7, 7);
        }

        DataSet dataSet = new DataSet(features, labels);
        ListDataSetIterator<DataSet> trainData = new ListDataSetIterator<>(dataSet.asList(), 32);

        // Normalize the data
        DataNormalization normalizer = new NormalizerMinMaxScaler(0, 1);
        normalizer.fit(trainData);
        trainData.setPreProcessor(normalizer);

        // Train for multiple epochs
        for (int i = 0; i < 10; i++) {  // 10 epochs
            model.fit(trainData);
            trainData.reset();
        }
    }

    public MultiLayerNetwork getModel() {
        return model;
    }

    public void setModel(MultiLayerNetwork model) {
        this.model = model;
    }

}