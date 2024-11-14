package game.mills;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

/**
 * The HumanPlayer class represents a player in the game.
 * It implements the Player interface and manages the player's name, color, and the number of stones
 * the player has to place and already placed on the board.
 */
public class HumanPlayer implements Player {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private Color color;
    @Getter
    @Setter
    private int stonesToPlace;
    @Getter
    @Setter
    private int stonesOnBoard;

    /**
     * Constructs a HumanPlayer with the specified name and color.
     * Each player starts with 9 stones to place on the board and none on the board initially.
     *
     * @param name  the name of the player.
     * @param color the color representing the player's stones.
     */
    public HumanPlayer(String name, Color color) {
        this.name = name;
        this.color = color;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
    }


    /**
     * Decrements the number of stones the player has left to place and increments the number of stones on the board.
     * Ensures the player has stones left to place before decrementing.
     */
    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

    /**
     * Increments the number of stones the player has on the board by one.
     */
    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    /**
     * Decrements the number of stones the player has on the board by one.
     */
    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }
}