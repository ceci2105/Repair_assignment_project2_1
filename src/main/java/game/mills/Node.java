package game.mills;

import javafx.scene.shape.Circle;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * The Node class represents a single position on the game board.
 * Each node can be occupied by a player and is associated with a visual circle in the UI.
 */
public class Node implements Serializable {
    /**
     * -- GETTER --
     * Returns the ID of a given Node.
     */
    @Getter
    /**
     *  -- SETTER --
     * Sets the ID of a given Node object.
     * @param id The id to be set as the ID.
     */
    @Setter
    private int id;             // Unique ID for each position on the board
    /**
     * -- SETTER --
     * Sets the player occupying this node.
     *
     * the player to set as the occupant of the node.
     */
    @Setter
    /**
     * -- GETTER --
     * Returns the occupant of a given Node.
     */
    @Getter
    private Player occupant;    // The player occupying the node (null if empty)
    /**
     * -- SETTER --
     * Sets the visual representation (circle) of this node in the UI.
     *
     *  the Circle object to associate with this node.
     */
    @Setter
    /**
     * -- GETTER --
     *  Returns the visual representation (circle) of this node in the UI.
     */
    @Getter
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