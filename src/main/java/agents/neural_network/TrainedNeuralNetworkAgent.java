package agents.neural_network;

import game.mills.Board;
import game.mills.Node;
import game.mills.Player;
import javafx.scene.paint.Color;
import lombok.Getter;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.*;
import org.tensorflow.types.TFloat16;



@Getter
public class TrainedNeuralNetworkAgent implements Player {
    private final SavedModelBundle model;
    private final String name;
    private final Color color;
    private int stonesToPlace;
    private int stonesOnBoard;

    public TrainedNeuralNetworkAgent(String modelID, Color color) {
        this.model = SavedModelBundle.load(modelID, "serve");
        this.name = modelID.concat(" AI Player");
        this.color = color;
    }

    public int[] selectMove(Board board) {
       return null;
    }

    protected Tensor convertBoardToTensor(Board board) {
        float[][] boardState = new float[1][24];
        for (int i = 0; i < 24; i++) {
            Node node = board.getNode(i);
            if (node.isOccupied()) {
                boardState[0][i] = node.getOccupant() == this ? 1 : 2;
            } else {
                boardState[0][i] = 0;
            }
        }
        float[][] adjMatrix = new float[24][24];
        for (int[] edge : board.getEdges()) {
            adjMatrix[edge[0]][edge[1]] = 1;
            adjMatrix[edge[0]][edge[1]] = 1;
        }

        float[] flattenedAdjMatrix = new float[24 * 24];
        int index = 0;
        for (float[] row : adjMatrix) {
            for (float value : row) {
                flattenedAdjMatrix[index++] = value;
            }
        }

        float[] combinedInput = new float[boardState[0].length + flattenedAdjMatrix.length];
        System.arraycopy(boardState[0], 0, combinedInput, 0, boardState[0].length);
        System.arraycopy(flattenedAdjMatrix, 0, combinedInput, boardState[0].length, flattenedAdjMatrix.length);
        FloatDataBuffer buffer = DataBuffers.ofFloats((long) combinedInput.length * (long) Float.BYTES);
        buffer.read(combinedInput);
        //TODO: Check for compatibility compared to TBFloat!
        return TFloat16.tensorOf(Shape.of(combinedInput.length), buffer);
    }


    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }
}


