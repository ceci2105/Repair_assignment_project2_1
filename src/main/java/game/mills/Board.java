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
 * Implementation using jgrapht
 * Handles all operations concerning the board:
 * Checking for a mill
 * Checking the neighbours
 * Checking if a move is valid
 */
public class Board {
    // Declaration of constants for better memory management and reusability.
    private static final int[][] edges = {{0, 1}, {1, 2}, {2, 14}, {14, 23}, {23, 22}, {22, 21}, {21, 9}, {9, 0}, {3, 4}, {4, 5}, {5, 13}, {13, 20}, {20, 19}, {19, 18}, {18, 10}, {10, 3}, {6, 7}, {7, 8}, {8, 12}, {12, 17}, {17, 16}, {16, 15}, {15, 11}, {11, 6}, {1, 4}, {4, 7}, {14, 13}, {13, 12}, {22, 19}, {19, 16}, {9, 10}, {10, 11}};
    private static final int[][] mills = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {15, 16, 17}, {18, 19, 20}, {21, 22, 23}, {0, 9, 21}, {3, 10, 18}, {6, 11, 15}, {1, 4, 7}, {16, 19, 22}, {8, 12, 17}, {5, 13, 20}, {2, 14, 23}, {9, 10, 11}, {12, 13, 14}};

    private final SimpleGraph<Integer, DefaultEdge> graph;
    private final Map<Integer, Node> nodes;

    public Board() {
        graph = new SimpleGraph<>(DefaultEdge.class);
        nodes = new HashMap<>();
        createNodes();
        createEdges();
    }

    /**
     * Created nodes for the Graph
     */
    private void createNodes() {
        for (int i = 0; i < 24; i++) {
            graph.addVertex(i);
            nodes.put(i, new Node(i));
        }
    }

    /**
     * Created edges based on the edges constant.
     */
    private void createEdges() {
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }

    }

    /**
     * Gets node at given ID
     *
     * @param id The ID of the Node
     * @return The Node at ID
     */
    public Node getNode(int id) {
        return nodes.get(id);
    }

    /**
     * Getter method for the edges constant
     *
     * @return The edges constant
     */
    public int[][] getEdges() {
        return edges;
    }

    /**
     * Gets the Mills constant.
     *
     * @return The Mills constant.
     */
    public int[][] getMills() {
        return mills;
    }

    /**
     * Gets the neighbours of a given node.
     *
     * @param node The node to fetch.
     * @return The neighbours of the node.
     */
    public List<Node> getNeighbours(Node node) {
        return Graphs.neighborListOf(graph, node.getId()).stream().map(nodes::get).collect(Collectors.toList());
    }

    /**
     * Checks if the move is Valid
     *
     * @param from The starting point of the move.
     * @param to   The end point of the move.
     * @return True if the move is valid, false otherwise.
     */
    public boolean isValidMove(Node from, Node to) {
        return graph.containsEdge(from.getId(), to.getId());
    }

    /**
     * Places a piece on the board.
     *
     * @param player The player that is placing
     * @param nodeID The Node where the piece will be placed.
     */
    public void placePiece(Player player, int nodeID) {
        Node node = getNode(nodeID);
        if (!node.isOccupied() && player.getStonesToPlace() > 0) {  // Only allow placement if player still has stones to place
            node.setOccupant(player);
            player.decrementStonesToPlace();  // This will both decrement stonesToPlace and increment stonesOnBoard
        }
    }

    /**
     * Moves a piece from a player from a given position to another.
     *
     * @param player The moving player.
     * @param fromID The given position.
     * @param toID   The target position.
     */
    public void movePiece(Player player, int fromID, int toID) {
        Node from = getNode(fromID);
        Node to = getNode(toID);

        if (from.getOccupant() == player && !to.isOccupied()) {
            from.setOccupant(null);
            to.setOccupant(player);
        }
    }

    public boolean checkMill(Node node, Player player) {
        return Arrays.stream(mills).parallel().anyMatch(mill -> {
            if (Arrays.stream(mill).anyMatch(id -> id == node.getId())) {
                return Arrays.stream(mill).allMatch(id -> nodes.get(id).getOccupant() == player);
            }
            return false;
        });
    }

    public boolean allOpponentStonesInMill(Player opponent) {
        return nodes.values().stream()
            .filter(node -> node.getOccupant() == opponent)
            .allMatch(node -> checkMill(node, opponent));
    }

    public boolean hasValidMoves(Player player) {
        for (Node node : nodes.values()) {
            // Check if the node belongs to the current player
            if (node.getOccupant() == player) {
                // Check if any of the neighbors are empty (valid move)
                for (Node neighbor : getNeighbours(node)) {
                    if (!neighbor.isOccupied()) {
                        return true; // Found a valid move
                    }
                }
            }
        }
        return false; // No valid moves found
    }
}
