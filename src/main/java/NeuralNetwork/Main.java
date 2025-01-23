package NeuralNetwork;

import NeuralNetwork.DataLoader;
import org.nd4j.linalg.api.ndarray.INDArray;

public class Main {
    public static void main(String[] args) throws Exception {
        INDArray[] data = DataLoader.loadTrainingData("training_data.csv");
        System.out.println("Inputs shape: " + data[0].shapeInfoToString());
        System.out.println("Labels shape: " + data[1].shapeInfoToString());
    }
}
