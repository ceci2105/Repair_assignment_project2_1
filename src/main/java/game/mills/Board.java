package game.mills;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Board class represents the game board.
 * It handles all operations concerning the board, including:
 * - Checking for mills
 * - Checking neighboring nodes
 * - Validating moves
 */
public class Board {
    // Declaration of constants for edges and mills.
    private static final int[][] edges = {{0, 1}, {1, 2}, {2, 14}, {14, 23}, {23, 22}, {22, 21}, {21, 9}, {9, 0}, {3, 4}, {4, 5}, {5, 13}, {13, 20}, {20, 19}, {19, 18}, {18, 10}, {10, 3}, {6, 7}, {7, 8}, {8, 12}, {12, 17}, {17, 16}, {16, 15}, {15, 11}, {11, 6}, {1, 4}, {4, 7}, {14, 13}, {13, 12}, {22, 19}, {19, 16}, {9, 10}, {10, 11}};
    private static final int[][] mills = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {15, 16, 17}, {18, 19, 20}, {21, 22, 23}, {0, 9, 21}, {3, 10, 18}, {6, 11, 15}, {1, 4, 7}, {16, 19, 22}, {8, 12, 17}, {5, 13, 20}, {2, 14, 23}, {9, 10, 11}, {12, 13, 14}};

    private final SimpleGraph<Integer, DefaultEdge> graph;
    private final Map<Integer, Node> nodes;

    /**
     * Constructs a new Board and initializes the game graph.
     * Creates the nodes and edges based on predefined constants.
     */
    public Board() {
        graph = new SimpleGraph<>(DefaultEdge.class);
        nodes = new HashMap<>();
        createNodes();
        createEdges();
    }

    /**
     * Creates nodes for the board and adds them to the graph.
     * Each node represents a position on the game board.
     */
    private void createNodes() {
        for (int i = 0; i < 24; i++) {
            graph.addVertex(i);
            nodes.put(i, new Node(i));
        }
    }

    /**
     * Creates edges between the nodes based on predefined constants.
     * These edges represent valid connections between the nodes on the game board.
     */
    private void createEdges() {
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }

    }

    /**
     * Retrieves the node at the specified ID.
     *
     * @param id The ID of the node.
     * @return The node at the specified ID.
     */
    public Node getNode(int id) {
        return nodes.get(id);
    }

    /**
     * Returns the edges constant, representing connections between nodes.
     *
     * @return The edges constant.
     */
    public int[][] getEdges() {
        return edges;
    }

    /**
     * Returns the mills constant, representing possible mills (three connected stones).
     *
     * @return The mills constant.
     */
    public int[][] getMills() {
        return mills;
    }

    /**
     * Gets the neighbors of a given node. These are nodes connected directly to the given node.
     *
     * @param node The node for which neighbors are to be fetched.
     * @return A list of neighboring nodes.
     */
    public List<Node> getNeighbours(Node node) {
        return Graphs.neighborListOf(graph, node.getId()).stream().map(nodes::get).collect(Collectors.toList());
    }

    /**
     * Validates if a move between two nodes is valid based on their adjacency.
     *
     * @param from The starting node of the move.
     * @param to   The destination node of the move.
     * @return True if the move is valid, false otherwise.
     */
    public boolean isValidMove(Node from, Node to) {
        return graph.containsEdge(from.getId(), to.getId());
    }

    /**
     * Places a player's stone on a specified node.
     *
     * @param player The player placing the stone.
     * @param nodeID The node where the stone will be placed.
     */
    public void placePiece(Player player, int nodeID) {
        Node node = getNode(nodeID);
        if (!node.isOccupied() && player.getStonesToPlace() > 0) {  // Only allow placement if humanPlayer still has stones to place
            node.setOccupant(player);
            player.decrementStonesToPlace();
        }
    }

   /**
     * Moves a player's stone from one node to another.
     *
     * @param player The player making the move.
     * @param fromID The starting node.
     * @param toID   The destination node.
     */
    public void movePiece(Player player, int fromID, int toID) {
        Node from = getNode(fromID);
        Node to = getNode(toID);

        if (from.getOccupant() == player && !to.isOccupied()) {
            from.setOccupant(null);
            to.setOccupant(player);
        }
    }

    /**
     * Checks if placing or moving a stone forms a mill (three consecutive stones).
     *
     * @param node   The node where the stone is placed or moved to.
     * @param player The player making the move.
     * @return True if the player forms a mill, false otherwise.
     */
    public boolean checkMill(Node node, Player player) {
        return Arrays.stream(mills).parallel().anyMatch(mill -> {
            if (Arrays.stream(mill).anyMatch(id -> id == node.getId())) {
                return Arrays.stream(mill).allMatch(id -> nodes.get(id).getOccupant() == player);
            }
            return false;
        });
    }

    /**
     * Checks if all of an opponent's stones are part of mills.
     * If all stones are in mills, the player can remove a mill stone.
     *
     * @param opponent The opponent whose stones are checked.
     * @return True if all of the opponent's stones are in mills, false otherwise.
     */
    public boolean allOpponentStonesInMill(Player opponent) {
        return nodes.values().stream()
            .filter(node -> node.getOccupant() == opponent)
            .allMatch(node -> checkMill(node, opponent));
    }

    /**
     * Checks if the player has any valid moves left.
     * A valid move is available if any neighboring node is unoccupied.
     *
     * @param player The player whose available moves are checked.
     * @return True if the player has at least one valid move, false otherwise.
     */
    public boolean hasValidMoves(Player player) {
        for (Node node : nodes.values()) {
            // Check if the node belongs to the current humanPlayer
            if (node.getOccupant() == player) {
                // Check if any of the neighbors are empty (valid move)
                for (Node neighbor : getNeighbours(node)) {
                    if (!neighbor.isOccupied()) {
                        return true;
                    }
                }
            }
        }
        return false; // No valid moves found
    }

    /**
     * Checks if a node is part of a mill.
     *
     * @param node The node to check.
     * @return True if the node is part of a mill, false otherwise.
     */
    public boolean isPartOfMill(Node node) {
        Player occupant = node.getOccupant();
        if (occupant == null) {
            return false;
        }
        return Arrays.stream(mills).anyMatch(mill -> Arrays.stream(mill).anyMatch(id -> id == node.getId()) &&
                Arrays.stream(mill).allMatch(id -> nodes.get(id).getOccupant() == occupant));
    }
}
