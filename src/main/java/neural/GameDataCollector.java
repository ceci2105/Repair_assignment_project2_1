package neural;

import game.mills.*;
import java.io.*;
import java.util.*;

public class GameDataCollector {
    private List<GameRecord> gameRecords;

    public GameDataCollector() {
        this.gameRecords = new ArrayList<>();
    }

    /**
     * Records a single game state and its outcome.
     * Should be called after each move in the game.
     */
    public void recordGameState(Board board, Player currentPlayer, boolean isGameOver, Player winner) {
        // Convert current board state to tensor format
        float[][][] boardState = BoardStateConverter.convertToTensor(board, currentPlayer);

        // Calculate game outcome from current player's perspective
        float outcome = calculateOutcome(currentPlayer, winner, isGameOver);

        // Create and store the position record
        BoardPosition position = new BoardPosition(boardState, outcome);

        // If this is the first position of a new game, create a new game record
        if (gameRecords.isEmpty() || gameRecords.get(gameRecords.size() - 1).isComplete()) {
            gameRecords.add(new GameRecord());
        }

        // Add position to current game record
        gameRecords.get(gameRecords.size() - 1).addPosition(position);

        // If game is over, mark the record as complete
        if (isGameOver) {
            gameRecords.get(gameRecords.size() - 1).setComplete(true);
            finalizeGameRecord(winner);
        }
    }

    private float calculateOutcome(Player currentPlayer, Player winner, boolean isGameOver) {
        if (!isGameOver) {
            return 0.0f; // Game in progress
        }
        if (winner == null) {
            return 0.0f; // Draw
        }
        return winner == currentPlayer ? 1.0f : -1.0f;
    }

    private void finalizeGameRecord(Player winner) {
        if (!gameRecords.isEmpty()) {
            GameRecord currentGame = gameRecords.get(gameRecords.size() - 1);
            currentGame.finalizePositions();
        }
    }

    public void saveGameData(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(gameRecords);
        }
    }

    public int getGamesCollected() {
        return (int) gameRecords.stream().filter(GameRecord::isComplete).count();
    }
}

/**
 * Represents a single game's worth of positions and their final outcome.
 */
class GameRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    // List of all positions that occurred in this game
    private final List<BoardPosition> positions;

    // Flag indicating if this game record is complete
    private boolean isComplete;

    // The final outcome of the game, used to adjust position evaluations
    private float finalOutcome;

    /**
     * Creates a new game record to store positions from a single game.
     */
    public GameRecord() {
        this.positions = new ArrayList<>();
        this.isComplete = false;
        this.finalOutcome = 0.0f;
    }

    /**
     * Adds a new board position to this game record.
     * 
     * @param position The board position to add
     */
    public void addPosition(BoardPosition position) {
        positions.add(position);
    }

    /**
     * Sets the completion status of this game record.
     * 
     * @param complete True if the game is finished, false otherwise
     */
    public void setComplete(boolean complete) {
        this.isComplete = complete;
    }

    /**
     * Checks if this game record is complete.
     * 
     * @return True if the game is finished, false otherwise
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Updates the evaluation scores for all positions in this game.
     * This method applies temporal difference learning principles by
     * discounting positions based on their distance from the game's end.
     */
    public void finalizePositions() {
        float discount = 0.95f; // Discount factor for earlier positions
        float currentValue = finalOutcome;

        // Work backwards from the end of the game
        for (int i = positions.size() - 1; i >= 0; i--) {
            positions.get(i).setEvaluation(currentValue);
            currentValue *= discount; // Reduce the impact for earlier positions
        }
    }

    /**
     * Sets the final outcome of the game. This value is used when finalizing
     * positions.
     * 
     * @param outcome The final game outcome (-1.0 for loss, 0.0 for draw, 1.0 for
     *                win)
     */
    public void setOutcome(float outcome) {
        this.finalOutcome = outcome;
    }

    /**
     * Gets the number of positions recorded in this game.
     * 
     * @return The number of positions
     */
    public int getPositionCount() {
        return positions.size();
    }

    /**
     * Gets all recorded positions from this game.
     * 
     * @return List of all recorded board positions
     */
    public List<BoardPosition> getPositions() {
        return new ArrayList<>(positions); // Return a copy to preserve encapsulation
    }
}

/**
 * Represents a single board position and its evaluation.
 * This class stores both the state of the board and its evaluated score.
 */
class BoardPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    // The board state represented as a 3D tensor
    private final float[][][] boardState;

    // The evaluation score for this position
    private float evaluation;

    /**
     * Creates a new board position with its evaluation.
     * 
     * @param boardState The board state as a 3D tensor
     * @param evaluation The initial evaluation score
     */
    public BoardPosition(float[][][] boardState, float evaluation) {
        this.boardState = boardState;
        this.evaluation = evaluation;
    }

    /**
     * Updates the evaluation score for this position.
     * 
     * @param evaluation The new evaluation score
     */
    public void setEvaluation(float evaluation) {
        this.evaluation = evaluation;
    }

    /**
     * Gets the board state tensor.
     * 
     * @return The 3D tensor representing the board state
     */
    public float[][][] getBoardState() {
        return boardState;
    }

    /**
     * Gets the evaluation score for this position.
     * 
     * @return The evaluation score
     */
    public float getEvaluation() {
        return evaluation;
    }
}
