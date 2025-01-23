package NeuralNetwork;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;

public class ModelLoader {

    public static MultiLayerNetwork loadModel(String filePath) throws IOException {
        return ModelSerializer.restoreMultiLayerNetwork(new File(filePath));
    }

    public static double evaluateBoardState(MultiLayerNetwork model, double[] boardState) {
        INDArray input = Nd4j.create(boardState);
        return model.output(input).getDouble(0);
    }
}
