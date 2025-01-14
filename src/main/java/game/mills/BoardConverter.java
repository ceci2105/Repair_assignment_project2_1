package game.mills;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class BoardConverter {
    // The board will be represented as a 7x7 grid
    private static final int BOARD_SIZE = 7;

    /**
     * Converts a Board instance to a tensor representation suitable for the CNN.
     * Creates a 3-channel 7x7 representation where:
     * Channel 0: Current player's pieces (1 where present, 0 elsewhere)
     * Channel 1: Opponent's pieces (1 where present, 0 elsewhere)
     * Channel 2: Empty spaces (1 where empty, 0 elsewhere)
     */
    public static INDArray boardToTensor(Board board, Player currentPlayer) {
        // Create a 3-channel 7x7 tensor
        INDArray tensor = Nd4j.zeros(1, 3, BOARD_SIZE, BOARD_SIZE);

        // Mapping from node IDs to 7x7 grid positions
        int[][] nodePositions = getNodePositions();

        // Fill the tensor based on the board state
        for (Node node : board.getNodes().values()) {
            int[] pos = nodePositions[node.getId()];
            if (pos != null) {
                if (node.getOccupant() == currentPlayer) {
                    tensor.putScalar(new int[] { 0, 0, pos[0], pos[1] }, 1.0);
                } else if (node.getOccupant() != null) {
                    tensor.putScalar(new int[] { 0, 1, pos[0], pos[1] }, 1.0);
                } else {
                    tensor.putScalar(new int[] { 0, 2, pos[0], pos[1] }, 1.0);
                }
            }
        }

        return tensor;
    }

    /**
     * Returns a mapping from node IDs to positions in the 7x7 grid.
     * This preserves the spatial relationship between nodes.
     */
    private static int[][] getNodePositions() {
        int[][] positions = new int[24][2];

        // Outer square
        positions[0] = new int[] { 0, 0 };
        positions[1] = new int[] { 0, 3 };
        positions[2] = new int[] { 0, 6 };
        positions[23] = new int[] { 3, 6 };
        positions[22] = new int[] { 6, 6 };
        positions[21] = new int[] { 6, 3 };
        positions[20] = new int[] { 6, 0 };
        positions[9] = new int[] { 3, 0 };

        // Middle square
        positions[3] = new int[] { 1, 1 };
        positions[4] = new int[] { 1, 3 };
        positions[5] = new int[] { 1, 5 };
        positions[13] = new int[] { 3, 5 };
        positions[19] = new int[] { 5, 5 };
        positions[18] = new int[] { 5, 3 };
        positions[17] = new int[] { 5, 1 };
        positions[10] = new int[] { 3, 1 };

        // Inner square
        positions[6] = new int[] { 2, 2 };
        positions[7] = new int[] { 2, 3 };
        positions[8] = new int[] { 2, 4 };
        positions[12] = new int[] { 3, 4 };
        positions[16] = new int[] { 4, 4 };
        positions[15] = new int[] { 4, 3 };
        positions[14] = new int[] { 4, 2 };
        positions[11] = new int[] { 3, 2 };

        return positions;
    }
}