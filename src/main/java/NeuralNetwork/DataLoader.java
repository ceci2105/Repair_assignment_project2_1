package NeuralNetwork;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DataLoader {

    public static INDArray[] loadTrainingData(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));

        String line;
        INDArray inputs = null;
        INDArray labels = null;

        int rowCount = 0;

        while ((line = br.readLine()) != null) {
            // Skip header
            if (rowCount == 0) {
                rowCount++;
                continue;
            }

            // Parse the row
            String[] parts = line.split(",");
            String boardStateStr = parts[0].replace("[", "").replace("]", "");
            double[] boardState = convertToDoubleArray(boardStateStr.split(","));
            double label = Double.parseDouble(parts[1]);

            // Initialize inputs and labels if they are null
            if (inputs == null) {
                inputs = Nd4j.create(new double[][]{boardState});
                labels = Nd4j.create(new double[]{label}).reshape(1, 1);
            } else {
                // Append data
                inputs = Nd4j.vstack(inputs, Nd4j.create(boardState));
                labels = Nd4j.vstack(labels, Nd4j.create(new double[]{label}).reshape(1, 1));
            }

            rowCount++;
        }

        br.close();

        if (inputs == null || labels == null) {
            throw new IllegalStateException("No data found in file: " + fileName);
        }

        return new INDArray[]{inputs, labels};
    }

    private static double[] convertToDoubleArray(String[] data) {
        return java.util.Arrays.stream(data).mapToDouble(Double::parseDouble).toArray();
    }
}
