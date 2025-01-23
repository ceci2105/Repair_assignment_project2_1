package NeuralNetwork;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;

public class ModelSaver {

    public static void saveModel(MultiLayerNetwork model, String filePath) throws IOException {
        ModelSerializer.writeModel(model, new File(filePath), true);
    }
}
