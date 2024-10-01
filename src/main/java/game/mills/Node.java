package game.mills;

import javafx.scene.shape.Circle;

/**
 * The Node class represents a single position on the game board.
 * Each node can be occupied by a player and is associated with a visual circle in the UI.
 */
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
     * Gets the unique ID of the node.
     *
     * @return the node's unique identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the player currently occupying this node.
     *
     * @return the player occupying the node, or null if the node is unoccupied.
     */
    public Player getOccupant() {
        return occupant;
    }

    /**
     * Sets the player occupying this node.
     *
     * @param player the player to set as the occupant of the node.
     */
    public void setOccupant(Player player) {
        this.occupant = player;
    }

    /**
     * Checks whether the node is currently occupied by a player.
     *
     * @return true if the node is occupied, false otherwise.
     */
    public boolean isOccupied() {
        return occupant != null;
    }

    /**
     * Gets the visual representation (circle) of this node in the UI.
     *
     * @return the Circle object associated with this node.
     */
    public Circle getCircle() {
        return circle;
    }

    /**
     * Sets the visual representation (circle) of this node in the UI.
     *
     * @param circle the Circle object to associate with this node.
     */
    public void setCircle(Circle circle) {
        this.circle = circle;
    }
}