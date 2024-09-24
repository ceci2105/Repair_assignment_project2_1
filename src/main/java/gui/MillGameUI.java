// MillGameUI.java
package gui;

import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class MillGameUI {

    private static final int CIRCLE_RADIUS = 15;
    private static final int BOARD_SIZE = 600;
    private Node selectedNode = null; // Store the selected node

    private Label statusLabel; // Label to display game status

    public MillGameUI(Stage primaryStage, Game game) {
        Pane root = new Pane();

        // Initialize the status label
        statusLabel = new Label("Game started. " + game.getCurrentPlayer().getName() + "'s turn.");
        statusLabel.setLayoutX(10);
        statusLabel.setLayoutY(BOARD_SIZE - 30);
        root.getChildren().add(statusLabel);

        // Coordinates for Nine Men's Morris positions
        double[][] positions = {
            {0.1, 0.1},  // Node 0
            {0.5, 0.1},  // Node 1
            {0.9, 0.1},  // Node 2
            {0.2, 0.2},  // Node 3
            {0.5, 0.2},  // Node 4
            {0.8, 0.2},  // Node 5
            {0.3, 0.3},  // Node 6
            {0.5, 0.3},  // Node 7
            {0.7, 0.3},  // Node 8
            {0.1, 0.5},  // Node 9
            {0.2, 0.5},  // Node 10
            {0.3, 0.5},  // Node 11
            {0.7, 0.5},  // Node 12
            {0.8, 0.5},  // Node 13
            {0.9, 0.5},  // Node 14
            {0.3, 0.7},  // Node 15
            {0.5, 0.7},  // Node 16
            {0.7, 0.7},  // Node 17
            {0.2, 0.8},  // Node 18
            {0.5, 0.8},  // Node 19
            {0.8, 0.8},  // Node 20
            {0.1, 0.9},  // Node 21
            {0.5, 0.9},  // Node 22
            {0.9, 0.9}   // Node 23
        };

        // Correctly defined edges between node indices
        int[][] edges = {
            // Outer square connections
            {0, 1}, {1, 2}, {2, 14}, {14, 23}, {23, 22}, {22, 21}, {21, 9}, {9, 0},
            // Middle square connections
            {3, 4}, {4, 5}, {5, 13}, {13, 20}, {20, 19}, {19, 18}, {18, 10}, {10, 3},
            // Inner square connections
            {6, 7}, {7, 8}, {8, 12}, {12, 17}, {17, 16}, {16, 15}, {15, 11}, {11, 6},
            // Vertical connections
            {0, 3}, {3, 6}, {1, 4}, {4, 7}, {2, 5}, {5, 8},
            {14, 13}, {13, 12}, {23, 20}, {20, 17}, {22, 19}, {19, 16}, {21, 18}, {18, 15}, {9, 10}, {10, 11}
        };

        // Draw the nodes (positions on the board)
        Circle[] circles = new Circle[positions.length];
        for (int i = 0; i < positions.length; i++) {
            // Get the node (position) from the board
            Node node = game.getBoard().getNode(i);

            // Create a circle to represent this node
            Circle circle = new Circle(CIRCLE_RADIUS);
            circle.setFill(Color.LIGHTGRAY);
            circle.setStroke(Color.BLACK);

            // Set the position of the circle on the pane
            circle.setCenterX(positions[i][0] * BOARD_SIZE);
            circle.setCenterY(positions[i][1] * BOARD_SIZE);

            // Handle clicks to select and move pieces
            int finalI = i;  // Final variable for lambda
            circle.setOnMouseClicked(event -> {
                handleClick(node, circle, game, finalI);
            });

            // Store the circle in the node for later updates
            node.setCircle(circle);

            // Add the circle to the root pane
            root.getChildren().add(circle);
            circles[i] = circle;
        }

        // Draw the edges (lines between nodes)
        for (int[] edge : edges) {
            int start = edge[0];
            int end = edge[1];

            // Create a line between two connected nodes
            Line line = new Line(
                circles[start].getCenterX(), circles[start].getCenterY(),
                circles[end].getCenterX(), circles[end].getCenterY()
            );
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2);

            // Add the line to the root pane
            root.getChildren().add(line);
        }

        // Set up the scene and stage
        Scene scene = new Scene(root, BOARD_SIZE, BOARD_SIZE);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Nine Men's Morris");
        primaryStage.show();
    }

    // Handle the selection and movement of nodes
    private void handleClick(Node node, Circle circle, Game game, int nodeIndex) {
        Player currentPlayer = game.getCurrentPlayer();

        if (game.isPlacingPhase()) {
            // Placing phase logic
            if (!node.isOccupied()) {
                game.placePiece(nodeIndex);

                // Update the UI
                if (node.getOccupant() != null) {
                    circle.setFill(node.getOccupant().getColor());
                }

                // Update the status label
                statusLabel.setText(game.getCurrentPlayer().getName() + "'s turn.");

            } else {
                System.out.println("Node already occupied.");
            }
        } else {
            // Moving phase logic
            if (selectedNode == null) {
                if (node.getOccupant() == currentPlayer) {
                    selectedNode = node;
                    circle.setStroke(Color.YELLOW); // Highlight selected piece
                } else {
                    System.out.println("Select your own piece to move.");
                }
            } else {
                if (!node.isOccupied() && (game.getBoard().isValidMove(selectedNode, node) || game.canFly(currentPlayer))) {
                    game.makeMove(selectedNode.getId(), node.getId());

                    // Update visuals
                    selectedNode.getCircle().setFill(Color.LIGHTGRAY);
                    selectedNode.getCircle().setStroke(Color.BLACK); // Remove highlight
                    circle.setFill(currentPlayer.getColor());

                    // Update the status label
                    statusLabel.setText(game.getCurrentPlayer().getName() + "'s turn.");

                    selectedNode = null;
                } else {
                    System.out.println("Invalid move.");
                    selectedNode.getCircle().setStroke(Color.BLACK); // Remove highlight
                    selectedNode = null;
                }
            }
        }
    }
}