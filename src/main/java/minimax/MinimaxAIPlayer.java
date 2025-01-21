package minimax;

import game.mills.*;
import gui.MillGameUI;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.Random;
import java.util.logging.Level;
import javafx.application.Platform;

/**
 * The MinimaxAIPlayer class represents an AI-controlled player that uses the Minimax algorithm
 * to make strategic moves in the game. It extends the Player interface to interact with the game,
 * and calculates moves based on the current board state, game phase, and opponent's position.
 */
@Log
public class MinimaxAIPlayer implements Player {
    private final int depth;            // The search depth for the Minimax algorithm
    @Setter
    private MinimaxAlgorithm minimax;   // Instance of MinimaxAlgorithm for calculating the best moves
    @Getter
    @Setter
    private String name;                // Name of the AI player
    @Getter
    @Setter
    private Color color;                // Color representing the AI player’s pieces on the board
    @Getter
    private int stonesToPlace;          // Stones the AI player still needs to place in the placement phase
    @Getter
    private int stonesOnBoard;          // Stones the AI player currently has on the board
    @Setter
    private Game game;                  // The current game instance

    /**
     * Constructor to initialize the MinimaxAIPlayer with a given name, depth, game, and color.
     *
     * @param name  The name of the AI player.
     * @param depth The search depth for the Minimax algorithm.
     * @param color The color representing the AI player’s pieces on the board.
     */
    public MinimaxAIPlayer(String name, Color color, int depth, Game game) {
        this.name = name;
        this.color = color;
        this.game = game;
        this.depth = depth;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
        EvaluationFunction evaluationFunction = new EvaluationFunction(game);
        this.minimax = new MinimaxAlgorithm(depth, evaluationFunction, game);
    }

    /**
     * Executes the best move calculated by the Minimax algorithm for the given board and phase.
     * The method finds the optimal move using Minimax and applies it to the board.
     *
     * @param board The game board on which the move is to be made.
     * @param phase The current phase of the game (1 = placement, 2 = movement, 3 = endgame).
     */
    public void makeMove(Board board, int phase) {
        Platform.runLater(() -> {
            if (stonesToPlace == 9) {
                // First move logic: place a random stone
                Random r = new Random();
                int randomPlacement = r.nextInt(24);
                if (board.getNode(randomPlacement).isOccupied()) {
                    randomPlacement = r.nextInt(24);
                    game.placePiece(randomPlacement);
                    MillGameUI.incrementMinimaxMoves();
                } else {
                    game.placePiece(randomPlacement);
                    MillGameUI.incrementMinimaxMoves();
                }
            } else {
                if (phase == 1) {
                    // Placement phase
                    int bestPlacement = minimax.findBestPlacement(board, this);
                    if (bestPlacement != -1) {
                        try {
                            game.placePiece(bestPlacement);
                            MillGameUI.incrementMinimaxMoves();
                            // Check for mill formation
                            if (game.isMillFormed()) {
                                handleMillFormation(board);
                            }
                        } catch (InvalidMove e) {
                            log.log(Level.WARNING, "Failed to place piece: {0}", e.getMessage());
                        }
                    }
                } else {
                    // Movement/Endgame phase
                    Node[] bestMove = minimax.findBestMove(board, this, phase);
                    if (bestMove != null && bestMove[0] != null && bestMove[1] != null) {
                        try {
                            game.makeMove(bestMove[0].getId(), bestMove[1].getId());
                            MillGameUI.incrementMinimaxMoves();
                            // Check for mill formation
                            if (game.isMillFormed()) {
                                handleMillFormation(board);
                            }
                        } catch (InvalidMove e) {
                            log.log(Level.WARNING, "Failed to make move: {0}", e.getMessage());
                        }
                    } else {
                        log.log(Level.WARNING, "No valid move found for AI.");
                        if (bestMove[0] == null || bestMove[1] == null) {
                            log.warning("No valid move found. Falling back to random.");
                            for (Node fromNode : board.getNodes().values()) {
                                if (fromNode.getOccupant() == this) {
                                    for (Node toNode : board.getNeighbours(fromNode)) {
                                        if (!toNode.isOccupied()) {
                                            game.makeMove(fromNode.getId(), toNode.getId());
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        
                    }
                }
            }
        });
    }

    /**
     * Decreases the number of stones the AI player has to place by one
     * and increments the stones on the board. This is used during the placement phase.
     */
    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

    /**
     * Increases the number of stones the AI player has on the board by one.
     * This is called when a new stone is added to the board.
     */
    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    /**
     * Decreases the number of stones the AI player has on the board by one.
     * This is typically called when a stone is removed from the board.
     */
    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }

    // Helper method to handle mill formation and remove opponent's piece
    // Helper method to handle mill formation and remove opponent's piece
    // Helper method to handle mill formation and remove opponent's piece
    private void handleMillFormation(Board board) {
        Player opponent = game.getOpponent(this);
        boolean removedPiece = false;

        // Iterate through the opponent's stones to remove a piece not in a mill
        for (Node node : board.getNodes().values()) {
            if (node.isOccupied() && node.getOccupant() == opponent && !board.isPartOfMill(node)) {
                try {
                    game.removePiece(node.getId());
                    log.info("Removed opponent's piece not in a mill.");
                    removedPiece = true;
                    break;
                } catch (InvalidMove e) {
                    log.log(Level.WARNING, "Failed to remove piece: {0}", e.getMessage());
                }
            }
        }

        // If all opponent stones are in mills, remove any one of them
        if (!removedPiece) {
            for (Node node : board.getNodes().values()) {
                if (node.isOccupied() && node.getOccupant() == opponent) {
                    try {
                        game.removePiece(node.getId());
                        log.info("All opponent stones are in mills. Removed a piece from a mill.");
                        break;
                    } catch (InvalidMove e) {
                        log.log(Level.WARNING, "Failed to remove piece from a mill: {0}", e.getMessage());
                    }
                }
            }
        }
    }



}
