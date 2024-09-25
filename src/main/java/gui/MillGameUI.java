// MillGameUI.java
package gui;

import game.mills.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class MillGameUI {
    static Board board;
    private static final int CIRCLE_RADIUS = 15;
    private static final int BOARD_SIZE = 600;
    private Node selectedNode = null; // Store the selected node

    private Label statusLabel; // Label to display game status

    public MillGameUI(Stage primaryStage, NewGame game) {
        board = game.getBoard();
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
        int[][] edges = board.getEdges();

        // First draw the edges (lines between nodes) so the pieces (circles) will be drawn on top
        for (int[] edge : edges) {
            int start = edge[0];
            int end = edge[1];

            // Create a line between two connected nodes
            Line line = new Line(
                positions[start][0] * BOARD_SIZE, positions[start][1] * BOARD_SIZE,
                positions[end][0] * BOARD_SIZE, positions[end][1] * BOARD_SIZE
            );
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2);

            // Add the line to the root pane
            root.getChildren().add(line);
        }

        // Then draw the nodes (positions on the board) after the lines so the circles are on top
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

            // Add the circle to the root pane after the lines
            root.getChildren().add(circle);
            circles[i] = circle;
        }

        // Set up the scene and stage
        Scene scene = new Scene(root, BOARD_SIZE, BOARD_SIZE);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Nine Men's Morris");
        primaryStage.show();
    }

    // Handle the selection and movement of nodes
    private void handleClick(Node node, Circle circle, NewGame game, int nodeIndex) {
        Player currentPlayer = game.getCurrentPlayer();

        if (game.isMillFormed()) {
            if (node.isOccupied() && node.getOccupant() != currentPlayer) {
                try {
                    game.removeOpponentStone(nodeIndex);
                    circle.setFill(Color.LIGHTGRAY); // Update UI after valid removal
                    updateGameStatus(currentPlayer.getName() + " removed an opponent's stone.");
        
                    // Update turn to reflect the switch to the next player after removal
                    updateGameStatus("Turn: " + game.getCurrentPlayer().getName());
                } catch (InvalidMove e) {
                    // If the removal is invalid, show the error message and prevent the stone removal
                    updateGameStatus(e.getMessage());
                }
            } else {
                updateGameStatus("Select a valid opponent's stone to remove.");
            }
            return;  // Exit after handling removal
        }

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
                // Allow any move if player can fly
                boolean canFly = game.canFly(currentPlayer);
                if (!node.isOccupied() && (game.getBoard().isValidMove(selectedNode, node) || canFly)) {
                    try {
                        game.makeMove(selectedNode.getId(), node.getId());

                        // Update visuals
                        selectedNode.getCircle().setFill(Color.LIGHTGRAY);
                        selectedNode.getCircle().setStroke(Color.BLACK); // Remove highlight
                        circle.setFill(currentPlayer.getColor());

                        // Update the status label
                        statusLabel.setText(game.getCurrentPlayer().getName() + "'s turn.");

                        selectedNode = null;
                    } catch (InvalidMove e) {
                        System.out.println(e.getMessage());
                        selectedNode.getCircle().setStroke(Color.BLACK); // Remove highlight
                        selectedNode = null;
                    }
                }
            }
        }
    }

    public void displayGameOverMessage(Player winner) {
        // Create a new Alert of type INFORMATION
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null); // You can set a header text if you want
        alert.setContentText("Game Over! " + winner.getName() + " wins!");

        // Disable further clicks or interactions after the game is over
        alert.setOnCloseRequest(event -> Platform.exit());

        // Show the alert and wait for the user to click OK
        alert.showAndWait();
    }


    public void updateGameStatus(String message) {
        statusLabel.setText(message);
    }


}