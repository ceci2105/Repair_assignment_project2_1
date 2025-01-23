package NeuralNetwork;

import game.mills.Board;
import game.mills.Node;
import game.mills.Player;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataCollector {

    private BufferedWriter dataWriter; // Writer for saving data incrementally
    private boolean isWriterInitialized = false; // Flag to track writer status
    private final List<String> gameDataBuffer = new ArrayList<>(); // Buffer to store multiple games
    private static final int BATCH_SIZE = 100; // Number of games to save in one batch

    // Initialize the writer (called once before starting the game)
    public void initDataWriter(String fileName) throws IOException {
        if (isWriterInitialized) {
            throw new IllegalStateException("Data writer is already initialized.");
        }
        dataWriter = new BufferedWriter(new FileWriter(fileName, true)); // Append mode
        isWriterInitialized = true;

        // Write header if the file is empty
        File file = new File(fileName);
        if (file.length() == 0) {
            dataWriter.write("BoardState,EvaluationScore\n");
            dataWriter.flush();
        }
    }

    // Record and save the board state and evaluation
    public void saveDataIncrementally(Board board, Player currentPlayer, int phase) throws IOException {
        if (!isWriterInitialized || dataWriter == null) {
            throw new IOException("Data writer is not initialized or already closed.");
        }

        INDArray boardArray = boardToINDArray(board, currentPlayer, null); // Convert board to INDArray

        int evalScore = evaluatonfunction.evaluate(board, currentPlayer, phase, null);

        // Format the data and add it to the buffer
        String dataLine = boardArray.toString().replace("\n", "") + "," + evalScore;
        gameDataBuffer.add(dataLine);

        // Flush the buffer to the file if it reaches the batch size
        if (gameDataBuffer.size() >= BATCH_SIZE) {
            flushBuffer();
        }
    }

    // Flush buffered data to the file
    private void flushBuffer() throws IOException {
        if (!isWriterInitialized || dataWriter == null) {
            throw new IOException("Data writer is not initialized or already closed.");
        }

        for (String dataLine : gameDataBuffer) {
            dataWriter.write(dataLine + "\n");
        }
        dataWriter.flush(); // Ensure data is written immediately
        gameDataBuffer.clear(); // Clear the buffer after flushing
    }

    // Close the writer (called at the end of the game)
    public void closeDataWriter() throws IOException {
        if (isWriterInitialized && dataWriter != null) {
            flushBuffer(); // Write any remaining data
            dataWriter.close();
            isWriterInitialized = false;
            dataWriter = null;
        } else {
            throw new IllegalStateException("Attempt to close an uninitialized or already closed writer.");
        }
    }

    // Convert the board to an INDArray representation (stub for integration)
    private INDArray boardToINDArray(Board board, Player player1, Player player2) {
        int gridSize = 7; // Define a fixed grid size (7x7)
        INDArray boardArray = Nd4j.zeros(gridSize, gridSize); // Create a 7x7 array initialized to 0

        // Map the 24 nodes to specific positions in the 7x7 grid
        int[][] nodePositions = {
                {0, 0}, {0, 3}, {0, 6}, // Top row
                {1, 1}, {1, 3}, {1, 5}, // Second row
                {2, 2}, {2, 3}, {2, 4}, // Third row
                {3, 0}, {3, 1}, {3, 2}, {3, 4}, {3, 5}, {3, 6}, // Middle row
                {4, 2}, {4, 3}, {4, 4}, // Fifth row
                {5, 1}, {5, 3}, {5, 5}, // Sixth row
                {6, 0}, {6, 3}, {6, 6}  // Bottom row
        };

        // Place node values in the corresponding grid positions
        Map<Integer, Node> nodes = board.getNodes(); // Assuming Board has a method getNodes() returning nodes
        for (int i = 0; i < nodePositions.length; i++) {
            int[] pos = nodePositions[i];
            Node node = nodes.get(i);

            if (node.isOccupied()) {
                Player occupant = node.getOccupant();
                if (occupant.equals(player1)) {
                    boardArray.putScalar(pos[0], pos[1], 1); // Mark as Player 1
                } else if (occupant.equals(player2)) {
                    boardArray.putScalar(pos[0], pos[1], 2); // Mark as Player 2
                }
            }
        }

        return boardArray;
    }


    // Mock evaluation function (stub for integration)
    private EvaluationFunction evaluatonfunction = new EvaluationFunction();

    private static class EvaluationFunction {
        public int evaluate(Board board, Player currentPlayer, int phase, Object extraParam) {
            // Your implementation for evaluating the board state
            return 0; // Replace with actual logic
        }
    }
}