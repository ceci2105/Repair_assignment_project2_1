package agents.neural_network;

import game.mills.Board;
import game.mills.Node;
import game.mills.Player;
import javafx.scene.paint.Color;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.ByteDataBuffer;
import org.tensorflow.ndarray.buffer.DataBuffer;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.proto.DataType;
import org.tensorflow.types.TFloat64;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;


public class NeuralNetworkAgent implements Player {
    private final SavedModelBundle model;
    private String name;
    private Color color;
    private int stonesToPlace;
    private int stonesOnBoard;

    public NeuralNetworkAgent(String modelPath) {
        this.model = SavedModelBundle.load(modelPath, "serve");
    }

    public int[] selectMove(Board board) {
        Tensor inputTensor = null;

        try (Session session = model.session()) {
            Tensor outputTensor = session.runner().feed("serving_default_input_1", inputTensor).fetch("StatefulPartitionedCall").run().get(0);
            int[] move = null;
            return move;
        }
    }

    private Tensor convertBoardToTensor(Board board) {
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
        for (int[] edge:board.getEdges()) {
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
        ByteBuffer byteBuffer = ByteBuffer.allocate(combinedInput.length * Float.BYTES);
        FloatDataBuffer floatBuffer = DataBuffer.of(combinedInput);
        return Tensor.of(TFloat64.class,Shape.of(combinedInput.length), buffer);
        }


    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public int getStonesToPlace() {
        return stonesToPlace;
    }

    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

    public int getStonesOnBoard() {
        return stonesOnBoard;
    }

    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }
}


