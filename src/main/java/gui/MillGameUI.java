package gui;

import Minimax.MinimaxAIPlayer;
import game.mills.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * The MillGameUI class represents the graphical user interface (GUI) for the game.
 * It handles creating the game board, pieces, and user interactions, as well as updating the game status.
 */
public class MillGameUI {
    private static final int CIRCLE_RADIUS = 15; // Radius of the game piece circles
    private static final int BOARD_SIZE = 600;
    private Node selectedNode = null; // Store the currently selected node

    private Label statusLabel; // Label to display the current game status
    private Stage primaryStage; // Store the primary stage
    private Game game; // Store the game instance
    private Board board; // Store the board instance

    /**
     * Constructor to initialize the MillGameUI.
     *
     * @param primaryStage The primary stage to display the game UI.
     */
    public MillGameUI(Stage primaryStage) {
        this.primaryStage = primaryStage;
        startNewGame();
    }

    /**
     * Starts a new game by initializing players, the game logic, and rebuilding the UI.
     */
    public void startNewGame() {
        selectedNode = null;
        // Initialize players with name and stone color
        HumanPlayer humanPlayer1 = new HumanPlayer("Player 1", Color.BLACK);
        HumanPlayer humanPlayer2 = new HumanPlayer("Player 2", Color.WHITE);
        //MinimaxAIPlayer minimaxAIPlayer = new MinimaxAIPlayer("AI Player", 3, game, Color.WHITE);

        // Initialize the game with the two players
        this.game = new Game(humanPlayer1, humanPlayer2);
        game.setUI(this);
        board = game.getBoard();

        // Build the UI
        buildUI();
    }

    /**
     * Builds the game UI components, including the board, pieces, and event handlers.
     */
    private void buildUI() {
        Pane root = new Pane();

        // Initialize the status label
        statusLabel = new Label("Game started. " + game.getCurrentPlayer().getName() + "'s turn.");
        statusLabel.setLayoutX(10);
        statusLabel.setLayoutY(BOARD_SIZE - 30);
        root.getChildren().add(statusLabel);

        // Coordinates for vertex positions
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

        // Get edges between nodes to draw connections
        int[][] edges = board.getEdges();

        // Drawing of edges
        for (int[] edge : edges) {
            int start = edge[0];
            int end = edge[1];

            // Creating a line between two connected nodes
            Line line = new Line(
                positions[start][0] * BOARD_SIZE, positions[start][1] * BOARD_SIZE,
                positions[end][0] * BOARD_SIZE, positions[end][1] * BOARD_SIZE
            );
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2);

            // Adding the line to the root pane
            root.getChildren().add(line);
        }

        // Drawing of nodes (on top of lines for visibility)
        Circle[] circles = new Circle[positions.length];
        for (int i = 0; i < positions.length; i++) {
            Node node = game.getBoard().getNode(i);

            // Creating a circle for each node
            Circle circle = new Circle(CIRCLE_RADIUS);
            circle.setFill(Color.LIGHTGRAY);
            circle.setStroke(Color.BLACK);

            // Setting of the circle on the pane
            circle.setCenterX(positions[i][0] * BOARD_SIZE);
            circle.setCenterY(positions[i][1] * BOARD_SIZE);

            // Handling clicks to select and move pieces
            int finalI = i;
            circle.setOnMouseClicked(event -> {
                handleClick(node, circle, finalI);
            });

            // Storing the circle in the node for later updates
            node.setCircle(circle);

            // Adding the circle to the root pane after the lines
            root.getChildren().add(circle);
            circles[i] = circle;
        }

        // Set up the scene and stage
        Scene scene = new Scene(root, BOARD_SIZE, BOARD_SIZE);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Nine Men's Morris");
        primaryStage.show();
    }

    /**
     * Handles user clicks for selecting and moving pieces on the board.
     * It manages the game logic based on the current game phase and player's actions.
     *
     * @param node       The node that was clicked.
     * @param circle     The graphical circle representing the node.
     * @param nodeIndex  The index of the clicked node.
     */
    private void handleClick(Node node, Circle circle, int nodeIndex) {
        Player currentPlayer = game.getCurrentPlayer();
        // If a mill is formed, the player can remove an opponent's stone
        if (game.isMillFormed()) {
            if (node.isOccupied() && node.getOccupant() != currentPlayer) {
                try {
                    game.removeOpponentStone(nodeIndex);
                    circle.setFill(Color.LIGHTGRAY);
                    updateGameStatus(currentPlayer.getName() + " removed an opponent's stone.");
                    updateGameStatus("Turn: " + game.getCurrentPlayer().getName());
                } catch (InvalidMove e) {
                    // If the removal is invalid, show the error message and prevent the stone removal
                    updateGameStatus(e.getMessage());
                }
            } else {
                updateGameStatus("Select a valid opponent's stone to remove.");
            }
            return;
        }

        // Handle the placing phase of the game
        if (game.isPlacingPhase()) {
            if (!node.isOccupied()) {
                try {
                    game.placePiece(nodeIndex);
                    if (node.getOccupant() != null) {
                        circle.setFill(node.getOccupant().getColor());
                    }
                    statusLabel.setText(game.getCurrentPlayer().getName() + "'s turn.");
                } catch (InvalidMove e) {
                    updateGameStatus(e.getMessage());
                }
            } else {
                updateGameStatus("Node already occupied.");
            }
        } else {
            // Moving phase logic
            if (selectedNode == null) {
                if (node.getOccupant() == currentPlayer) {
                    selectedNode = node;
                    circle.setStroke(Color.YELLOW); // Highlight selected piece
                } else {
                    updateGameStatus("Select your own piece to move.");
                }
            } else if (selectedNode == node) {
                // Deselect the piece if the same node is clicked again
                selectedNode.getCircle().setStroke(Color.BLACK); // Remove highlight
                selectedNode = null; // Unselect the node
                updateGameStatus("Piece deselected.");
            } else {
                // Allow any move if player can fly
                boolean canFly = game.canFly(currentPlayer);
                if (!node.isOccupied() && (game.getBoard().isValidMove(selectedNode, node) || canFly)) {
                    try {
                        game.makeMove(selectedNode.getId(), node.getId());
                        selectedNode.getCircle().setFill(Color.LIGHTGRAY);
                        selectedNode.getCircle().setStroke(Color.BLACK); // Remove highlight
                        circle.setFill(currentPlayer.getColor());
                        statusLabel.setText(game.getCurrentPlayer().getName() + "'s turn.");
                        selectedNode = null;
                    } catch (InvalidMove e) {
                        updateGameStatus(e.getMessage());
                        selectedNode.getCircle().setStroke(Color.BLACK); // Remove highlight
                        selectedNode = null;
                    }
                } else {
                    updateGameStatus("Invalid move.");
                }
            }
        }
    }

    /**
     * Displays a game over message when a player wins the game.
     *
     * @param winner The player who won the game.
     */
    public void displayGameOverMessage(Player winner) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("Game Over! " + winner.getName() + " wins!");

        ButtonType restartButton = new ButtonType("Restart");
        ButtonType exitButton = new ButtonType("Exit");

        // Set the custom buttons
        alert.getButtonTypes().setAll(restartButton, exitButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == restartButton) {
                restartGame();
            } else if (result.get() == exitButton) {
                Platform.exit();
            }
        }
    }

    /**
     * Restarts the game by re-initializing the game logic and UI.
     */
    private void restartGame() {
        selectedNode = null;
        startNewGame();
    }

    /**
     * Updates the game status label with a given message.
     *
     * @param message The message to be displayed in the status label.
     */
    public void updateGameStatus(String message) {
        statusLabel.setText(message);
    }
}