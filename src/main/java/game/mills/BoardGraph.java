// BoardGraph.java
package game.mills;

import java.util.*;
public class BoardGraph {
    private Map<Node, List<Node>> graph = new HashMap<>();
    private List<Node> nodes = new ArrayList<>();

    public BoardGraph() {
        createNodes();
        createEdges();
    }

    // Create 24 nodes corresponding to the valid positions on the Mills board
    private void createNodes() {
        for (int i = 0; i < 24; i++) {
            Node node = new Node(i);
            nodes.add(node);
            graph.put(node, new ArrayList<>());
        }
    }

    // Define the valid connections between positions (edges)
    private void createEdges() {
        int[][] edges = {
            // Outer square connections
            {0, 1}, {1, 2}, {2, 14}, {14, 23}, {23, 22}, {22, 21}, {21, 9}, {9, 0},
            // Middle square connections
            {3, 4}, {4, 5}, {5, 13}, {13, 20}, {20, 19}, {19, 18}, {18, 10}, {10, 3},
            // Inner square connections
            {6, 7}, {7, 8}, {8, 12}, {12, 17}, {17, 16}, {16, 15}, {15, 11}, {11, 6},
            // Vertical connections (connecting middle of each side of squares)
            {0, 3}, {3, 6}, {1, 4}, {4, 7}, {2, 5}, {5, 8},
            {14, 13}, {13, 12}, {23, 20}, {20, 17}, {22, 19}, {19, 16}, {21, 18}, {18, 15}, {9, 10}, {10, 11}
        };

        for (int[] edge : edges) {
            connect(edge[0], edge[1]);
        }
    }

    // Helper method to add an edge between two nodes
    private void connect(int id1, int id2) {
        Node node1 = nodes.get(id1);
        Node node2 = nodes.get(id2);
        graph.get(node1).add(node2);
        graph.get(node2).add(node1);
    }

    // Get a node by its ID
    public Node getNode(int id) {
        return nodes.get(id);
    }

    // Get all valid neighboring nodes (for move validation)
    public List<Node> getNeighbors(Node node) {
        return graph.get(node);
    }

    // Check if a move is valid (i.e., if there's an edge between two nodes)
    public boolean isValidMove(Node from, Node to) {
        return graph.get(from).contains(to) && !to.isOccupied();
    }

    // Check for a mill (three pieces in a row) based on connected nodes
    public boolean checkMill(Node node, Player player) {
        // Define all possible mills that include this node
        int[][] mills = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {15, 16, 17}, {18, 19, 20}, {21, 22, 23},
            {0, 9, 21}, {3, 10, 18}, {6, 11, 15},
            {1, 4, 7}, {16, 19, 22}, {8, 12, 17},
            {5, 13, 20}, {2, 14, 23}, {9, 10, 11},
            {12, 13, 14}
        };

        for (int[] mill : mills) {
            if (containsNode(mill, node.getId())) {
                boolean isMill = true;
                for (int id : mill) {
                    if (nodes.get(id).getOccupant() != player) {
                        isMill = false;
                        break;
                    }
                }
                if (isMill) {
                    return true;
                }
            }
        }
        return false;
    }

    // Helper method to check if an array contains a node ID
    private boolean containsNode(int[] array, int nodeId) {
        for (int id : array) {
            if (id == nodeId) {
                return true;
            }
        }
        return false;
    }
}