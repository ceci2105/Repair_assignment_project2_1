// Node.java
package game.mills;

import javafx.scene.shape.Circle;

public class Node {
    private int id;             // Unique ID for each position on the board
    private Player occupant;    // The player occupying the node (null if empty)
    private Circle circle;      // Reference to the circle in the UI

    public Node(int id) {
        this.id = id;
        this.occupant = null;
    }

    public int getId() {
        return id;
    }

    public Player getOccupant() {
        return occupant;
    }

    public void setOccupant(Player player) {
        this.occupant = player;
    }

    public boolean isOccupied() {
        return occupant != null;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }
}