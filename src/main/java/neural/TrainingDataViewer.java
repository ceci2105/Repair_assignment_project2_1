package neural;

import java.io.*;
import java.util.List;

/**
 * Utility class for examining collected training data from .ser files.
 * This class helps analyze and visualize the game positions and outcomes
 * that were collected during the training data generation process.
 */
public class TrainingDataViewer {
    /**
     * Loads and displays statistics about the collected training data.
     * 
     * @param filename The path to the .ser file containing the training data
     */
    public static void analyzeTrainingData(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            @SuppressWarnings("unchecked")
            List<GameRecord> gameRecords = (List<GameRecord>) ois.readObject();

            // Print basic statistics
            System.out.println("Training Data Analysis");
            System.out.println("=====================");
            System.out.println("Total games collected: " + gameRecords.size());

            int totalPositions = 0;
            int winsCount = 0;
            int lossesCount = 0;
            int drawsCount = 0;

            // Analyze each game
            for (GameRecord game : gameRecords) {
                totalPositions += game.getPositionCount();

                // Get the final position's evaluation to determine game outcome
                if (!game.getPositions().isEmpty()) {
                    float finalEval = game.getPositions().get(game.getPositions().size() - 1).getEvaluation();
                    if (finalEval > 0.5)
                        winsCount++;
                    else if (finalEval < -0.5)
                        lossesCount++;
                    else
                        drawsCount++;
                }
            }

            // Print detailed statistics
            System.out.println("\nDetailed Statistics:");
            System.out.println("------------------");
            System.out.println("Total positions collected: " + totalPositions);
            System.out.println("Average positions per game: " +
                    String.format("%.2f", (double) totalPositions / gameRecords.size()));
            System.out.println("\nGame Outcomes:");
            System.out.println("Wins: " + winsCount +
                    String.format(" (%.1f%%)", (100.0 * winsCount / gameRecords.size())));
            System.out.println("Losses: " + lossesCount +
                    String.format(" (%.1f%%)", (100.0 * lossesCount / gameRecords.size())));
            System.out.println("Draws: " + drawsCount +
                    String.format(" (%.1f%%)", (100.0 * drawsCount / gameRecords.size())));

            // Sample some positions
            System.out.println("\nSample Positions from First Game:");
            if (!gameRecords.isEmpty() && !gameRecords.get(0).getPositions().isEmpty()) {
                GameRecord firstGame = gameRecords.get(0);
                int samplesToShow = Math.min(3, firstGame.getPositionCount());

                for (int i = 0; i < samplesToShow; i++) {
                    BoardPosition pos = firstGame.getPositions().get(i);
                    System.out.println("\nPosition " + (i + 1) + ":");
                    printBoardState(pos.getBoardState());
                    System.out.println("Evaluation: " + pos.getEvaluation());
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading training data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints a visual representation of a board state to the console.
     * This helps verify that the collected positions make sense.
     */
    private static void printBoardState(float[][][] boardState) {
        // Print the player pieces channel
        System.out.println("Board State (Player pieces shown as X, opponent as O):");
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                if (boardState[2][i][j] > 0.5) { // Valid position check
                    if (boardState[0][i][j] > 0.5)
                        System.out.print("X ");
                    else if (boardState[1][i][j] > 0.5)
                        System.out.print("O ");
                    else
                        System.out.print(". ");
                } else {
                    System.out.print("  "); // Invalid position
                }
            }
            System.out.println();
        }
    }

    /**
     * Main method for running the analysis.
     */
    public static void main(String[] args) {
        // Specify the path to your training data file
        String dataFile = "C:\\Users\\Utilisateur\\Desktop\\Project2\\Project_2_1\\training_data.ser";
        analyzeTrainingData(dataFile);
    }
}