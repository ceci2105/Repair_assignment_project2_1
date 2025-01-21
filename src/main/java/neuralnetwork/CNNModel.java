package neuralnetwork;

import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
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
                        .nIn(4)
                        .stride(1, 1)
                        .nOut(16)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new ConvolutionLayer.Builder(3, 3)
                        .nOut(32)
                        .stride(1, 1)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nOut(1)
                        .activation(Activation.IDENTITY)
                        .build())
                .setInputType(InputType.convolutional(7, 7, 4))
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
    }

    public float evaluatePosition(float[][][] boardTensor) {
        INDArray input = Nd4j.create(boardTensor);
        INDArray output = model.output(input);
        return output.getFloat(0);
    }

    public void train(INDArray features, INDArray labels) {
        DataSet dataSet = new DataSet(features, labels);

        ListDataSetIterator<DataSet> trainData = new ListDataSetIterator<>(dataSet.asList(), 10);

        DataNormalization normalizer = new NormalizerMinMaxScaler(0, 1);
        normalizer.fit(trainData);
        trainData.setPreProcessor(normalizer);

        model.fit(trainData);
    }
}