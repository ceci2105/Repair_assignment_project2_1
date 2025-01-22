package neuralnetwork;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.api.Layer;

public class ModelInspector {
    public static void inspectModel(String modelPath) {
        try {
            // Load the model
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(modelPath);

            System.out.println("Model Structure:");
            System.out.println("-----------------");

            // Get configuration for each layer
            Layer[] layers = model.getLayers();

            for (int i = 0; i < layers.length; i++) {
                System.out.println("\nLayer " + i + ":");

                Layer layer = layers[i];
                System.out.println("Type: " + layer.getClass().getSimpleName());

                // Print layer parameters shape
                System.out.println("Parameters shape: " + layer.params().shapeInfoToString());
                System.out.println("Number of parameters: " + layer.numParams());

                // Get layer configuration based on type
                if (layer instanceof ConvolutionLayer) {
                    ConvolutionLayer convLayer = (ConvolutionLayer) layer;
                    System.out.println("Kernel size: " + java.util.Arrays.toString(convLayer.getKernelSize()));
                    System.out.println("Stride: " + java.util.Arrays.toString(convLayer.getStride()));
                    System.out.println("Number of filters: " + convLayer.getNOut());
                }
                else if (layer instanceof SubsamplingLayer) {
                    SubsamplingLayer poolLayer = (SubsamplingLayer) layer;
                    System.out.println("Pool size: " + java.util.Arrays.toString(poolLayer.getKernelSize()));
                    System.out.println("Stride: " + java.util.Arrays.toString(poolLayer.getStride()));
                }
                else if (layer instanceof DenseLayer) {
                    DenseLayer denseLayer = (DenseLayer) layer;
                    System.out.println("Input size: " + denseLayer.getNIn());
                    System.out.println("Output size: " + denseLayer.getNOut());
                }
            }

            System.out.println("\nTotal model parameters: " + model.numParams());

        } catch (Exception e) {
            System.err.println("Error loading model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        inspectModel("models/cnn_model_1737545479340.zip");
    }
}
