package game.mills;

import lombok.Getter;
import lombok.extern.java.Log;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * The Board class represents the game board.
 * It handles all operations concerning the board, including:
 * - Checking for mills
 * - Checking neighboring nodes
 * - Validating moves
 */
@Log
public class Board {
    // Declaration of constants for edges and mills
    @Getter
    private static final int[][] edges = {{0, 1}, {1, 2}, {2, 14}, {14, 23}, {23, 22}, {22, 21}, {21, 9}, {9, 0}, {3, 4}, {4, 5}, {5, 13}, {13, 20}, {20, 19}, {19, 18}, {18, 10}, {10, 3}, {6, 7}, {7, 8}, {8, 12}, {12, 17}, {17, 16}, {16, 15}, {15, 11}, {11, 6}, {1, 4}, {4, 7}, {14, 13}, {13, 12}, {22, 19}, {19, 16}, {9, 10}, {10, 11}};
    @Getter
    private static final int[][] mills = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {15, 16, 17}, {18, 19, 20}, {21, 22, 23}, {0, 9, 21}, {3, 10, 18}, {6, 11, 15}, {1, 4, 7}, {16, 19, 22}, {8, 12, 17}, {5, 13, 20}, {2, 14, 23}, {9, 10, 11}, {12, 13, 14}};
    private static final long serialVersionUID = 1L;
    private final SimpleGraph<Integer, DefaultEdge> graph;
    @Getter
    private Map<Integer, Node> nodes;

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
     * Gets the neighbors of a given node. These are nodes connected directly to the given node.
     *
     * @param node The node for which neighbors are to be fetched.
     * @return A list of neighboring nodes.
     */
    public List<Node> getNeighbours(Node node) {
        List<Node> neighbours = new ArrayList<>();
        for(Integer neighbourID : Graphs.neighborListOf(graph, node.getId())) {
            neighbours.add(nodes.get(neighbourID));
        }
        return neighbours;
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
     * Places a player's stone on a specified node.
     *
     * @param player The player placing the stone.
     * @param nodeID The node where the stone will be placed.
     */
    public void placePieceAgent(Player player, int nodeID) {
        Node node = getNode(nodeID);
        if (!node.isOccupied() && player.getStonesToPlace() > 0) {  // Only allow placement if humanPlayer still has stones to place
            node.setOccupant(player);
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
    for (int[] mill : mills) {
        boolean isMill = false;
        for (int id : mill) {
            if (id == node.getId()) {
                isMill = true;
                break;
            }
        }
        if (isMill) {
            boolean allMatch = true;
            for (int id : mill) {
                if (nodes.get(id).getOccupant() != player) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                return true;
            }
        }
    }
    return false;
}

    /**
     * Checks if all of an opponent's stones are part of mills.
     * If all stones are in mills, the player can remove a mill stone.
     *
     * @param opponent The opponent whose stones are checked.
     * @return True if all the opponent's stones are in mills, false otherwise.
     */
    public boolean allOpponentStonesInMill(Player opponent) {
        return nodes.values().stream().parallel()
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
     * Gets all the neighbouring nodes from a given node of a given player.
     * @param nodeID The node to check.
     * @param player The Player.
     * @return The count of neighbouring nodes.
     */
    public int getPlayerNeighbours(int nodeID, Player player) {

        return (int) Graphs.neighborListOf(graph, nodeID).stream()
                .parallel()
                .map(nodes::get)
                .filter(neighbour -> neighbour.getOccupant() == player)
                .count();

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
        return Arrays.stream(mills).parallel().anyMatch(mill -> Arrays.stream(mill).anyMatch(id -> id == node.getId()) &&
                Arrays.stream(mill).parallel().allMatch(id -> nodes.get(id).getOccupant() == occupant));
    }

    /**
     * Copies a given board one to one.
     * @return The copied board.
     */
    public Board deepCopy() {
        Board copy = new Board();
        copy.createEdges();
        copy.nodes = new HashMap<>();
        for (Map.Entry<Integer, Node> entry : this.nodes.entrySet()) {
            Node originalNode = entry.getValue();
            Node copiedNode = new Node(originalNode.getId());
            copiedNode.setOccupant(originalNode.getOccupant());
            copiedNode.setCircle(originalNode.getCircle());
            copy.nodes.put(entry.getKey(), copiedNode);
        }
        return copy;
    }

    /**
     * Tests if a given stone placement will form a mill.
     * @param node The node of the given stone.
     * @param opponent The opponent of the given player.
     * @param board The game Board.
     * @return True if the placement will form a mill, false otherwise.
     */
    public boolean willFormMill(Node node, Player opponent, Board board) {
        return Arrays.stream(board.getMills()).parallel().anyMatch(mill -> {
            if (Arrays.stream(mill).parallel().anyMatch(id -> id == node.getId())) {
                return Arrays.stream(mill)
                        .parallel()
                        .filter(id -> id != node.getId())
                        .allMatch(id -> board.getNode(id).getOccupant() == opponent);
            }
            return false;
        });
    }

    /**
     * Finds the Nodes at which the Opponent might form a mill (2 neighboured stoned)
     * @param board The Game board.
     * @param opponent The opponent of the Player.
     * @return A list containing all nodes based on which a mill could be formed.
     */
    public List<Node> findPossibleMills(Board board, Player opponent) {
        List<Node> possibleMills = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (node.getOccupant() == opponent && getPlayerNeighbours(node.getId(), opponent) == 2) {
                possibleMills.add(node);
            }
        }
        return possibleMills;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Node node : nodes.values()) {
            if (node.isOccupied()) {
                sb.append(node.getOccupant().getName().charAt(0)); // Use 'B' for Black, 'W' for White, etc.
            } else {
                sb.append("0"); // Use '0' for empty spaces
            }
        }
        return sb.toString();
    }






}
