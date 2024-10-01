package game.mills;

import javafx.scene.paint.Color;

/**
 * The Player interface defines the essential moves and parameters for any player of the game.
 * It includes methods for retrieving player information, managing the number of stones a player has, and tracking the game state for the player.
 */
public interface Player {
    /**
     * Gets the name of the player.
     * 
     * @return the name of the player as a String.
     */
    String getName();

    /**
     * Gets the color representing the player's pieces.
     * 
     * @return the color of the player's pieces.
     */
    Color getColor();

    /**
     * Gets the number of stones the player still has to place on the board.
     * 
     * @return the number of stones left to place.
     */
    int getStonesToPlace();

    /**
     * Gets the number of stones the player currently has on the board.
     * 
     * @return the number of stones on the board.
     */
    int getStonesOnBoard();

    /**
     * Decreases the number of stones the player has left to place by one.
     */
    void decrementStonesToPlace();

    /**
     * Increases the number of stones the player has on the board by one.
     */
    void incrementStonesOnBoard();

    /**
     * Decreases the number of stones the player has on the board by one.
     */
    void decrementStonesOnBoard();
}
