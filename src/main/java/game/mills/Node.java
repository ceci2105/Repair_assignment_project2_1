package game.mills;

import javafx.scene.shape.Circle;
import lombok.Getter;
import lombok.Setter;

/**
 * The Node class represents a single position on the game board.
 * Each node can be occupied by a player and is associated with a visual circle in the UI.
 */
@Setter
@Getter
public class Node {
    private int id;             // Unique ID for each position on the board
    private Player occupant;    // The player occupying the node (null if empty)
    private Circle circle;      // Reference to the circle representing the node in the UI

    /**
     * Constructor to initialize a Node with a unique ID.
     *
     * @param id the unique identifier for this node.
     */
    public Node(int id) {
        this.id = id;
        this.occupant = null;
    }


    /**
     * Checks whether the node is currently occupied by a player.
     *
     * @return true if the node is occupied, false otherwise.
     */
    public boolean isOccupied() {
        return occupant != null;
    }


}