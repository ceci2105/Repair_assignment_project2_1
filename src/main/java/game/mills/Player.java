// Player.java
package game.mills;

import javafx.scene.paint.Color;

public class Player {
    private String name;
    private Color color;
    private int stonesToPlace;
    private int stonesOnBoard;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.stonesToPlace = 9; // Each player starts with 9 stones to place
        this.stonesOnBoard = 0; // Number of stones currently on the board
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