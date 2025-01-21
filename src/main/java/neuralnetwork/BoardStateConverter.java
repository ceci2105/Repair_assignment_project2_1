package neuralnetwork;

import game.mills.Board;
import game.mills.Node;
import game.mills.Player;

/**
 * Handles the conversion of the Nine Men's Morris board state into a format
 * suitable for CNN processing. The board is represented as multiple channels
 * of 7x7 matrices to capture different aspects of the game state.
 */
public class BoardStateConverter {
    private static final int BOARD_SIZE = 7;
    private static final int NUM_CHANNELS = 4; // Multiple channels for different features

    /**
     * Converts the current board state into a tensor representation suitable for
     * CNN input.
     *
     * Channel 0: Player's pieces (1 for player's pieces, 0 otherwise)
     * Channel 1: Opponent's pieces (-1 for opponent's pieces, 0 otherwise)
     * Channel 2: Valid positions (1 for valid board positions, 0 for invalid
     * spaces)
     * Channel 3: Mill positions (1 for positions part of a mill, 0 otherwise)
     *
     * @param board  The current game board
     * @param player The player from whose perspective we're converting
     * @return A 3D float array representing the board state [channels][rows][cols]
     */
    public static float[][][] convertToTensor(Board board, Player player) {
        float[][][] tensor = new float[NUM_CHANNELS][BOARD_SIZE][BOARD_SIZE];

        // Initialize valid positions channel (constant for all games)
        initializeValidPositions(tensor[2]);

        // Fill player and opponent pieces
        for (Node node : board.getNodes().values()) {
            int[] coords = nodeIdToCoordinates(node.getId());
            if (coords != null) {
                if (node.getOccupant() == player) {
                    tensor[0][coords[0]][coords[1]] = 1.0f;
                } else if (node.getOccupant() != null) {
                    tensor[1][coords[0]][coords[1]] = -1.0f;
                }

                // Mark mill positions
                if (board.isPartOfMill(node)) {
                    tensor[3][coords[0]][coords[1]] = 1.0f;
                }
            }
        }

        return tensor;
    }

    /**
     * Converts a node ID to 2D coordinates on the 7x7 grid.
     * This mapping is specific to the standard Nine Men's Morris board layout.
     */
    private static int[] nodeIdToCoordinates(int nodeId) {
        // Mapping of node IDs to 7x7 grid coordinates
        switch (nodeId) {
            case 0:
                return new int[] { 0, 0 };
            case 1:
                return new int[] { 0, 3 };
            case 2:
                return new int[] { 0, 6 };
            case 3:
                return new int[] { 1, 1 };
            case 4:
                return new int[] { 1, 3 };
            case 5:
                return new int[] { 1, 5 };
            // ... add remaining mappings
            default:
                return null;
        }
    }

    /**
     * Initializes the valid positions channel with 1s for valid board positions
     * and 0s for invalid spaces.
     */
    private static void initializeValidPositions(float[][] validPositions) {
        // Mark valid positions with 1.0f, leaving others as 0.0f
        for (int[] coords : VALID_POSITIONS) {
            validPositions[coords[0]][coords[1]] = 1.0f;
        }
    }

    private static final int[][] VALID_POSITIONS = {
            { 0, 0 }, { 0, 3 }, { 0, 6 },
            { 1, 1 }, { 1, 3 }, { 1, 5 },
            { 2, 2 }, { 2, 3 }, { 2, 4 },
            { 3, 0 }, { 3, 1 }, { 3, 2 }, { 3, 4 }, { 3, 5 }, { 3, 6 },
            { 4, 2 }, { 4, 3 }, { 4, 4 },
            { 5, 1 }, { 5, 3 }, { 5, 5 },
            { 6, 0 }, { 6, 3 }, { 6, 6 }
    };
}