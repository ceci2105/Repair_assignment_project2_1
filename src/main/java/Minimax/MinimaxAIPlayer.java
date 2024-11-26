package Minimax;

import game.mills.*;
import gui.MillGameUI;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.Random;
import java.util.logging.Level;

/**
 * The MinimaxAIPlayer class represents an AI-controlled player that uses the Minimax algorithm
 * to make strategic moves in the game. It extends the Player interface to interact with the game,
 * and calculates moves based on the current board state, game phase, and opponent's position.
 */
@Log
public class MinimaxAIPlayer implements Player {
    private final int depth;                   // The search depth for the Minimax algorithm
    private final MinimaxAlgorithm minimax;    // Instance of MinimaxAlgorithm for calculating the best moves
    @Getter
    @Setter
    private String name;                 // Name of the AI player
    @Getter
    @Setter
    private Color color;                 // Color representing the AI player’s pieces on the board
    @Getter
    private int stonesToPlace;           // Stones the AI player still needs to place in the placement phase
    @Getter
    private int stonesOnBoard;           // Stones the AI player currently has on the board
    @Setter
    private Game game;                   // The current game instance

    /**
     * Constructor to initialize the MinimaxAIPlayer with a given name, depth, game, and color.
     *
     * @param name  The name of the AI player.
     * @param depth The search depth for the Minimax algorithm.
     * @param game  The game instance for accessing board and opponent information.
     * @param color The color representing the AI player’s pieces on the board.
     */
    public MinimaxAIPlayer(String name, Color color, Game game, int depth) {
        this.name = name;
        this.color = color;
        this.depth = depth;
        this.game = game;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
        this.minimax = new MinimaxAlgorithm(game, depth); // Initialize the Minimax algorithm
    }


    /**
     * Executes the best move calculated by the Minimax algorithm for the given board and phase.
     * The method finds the optimal move using Minimax and applies it to the board.
     *
     * @param board The game board on which the move is to be made.
     * @param phase The current phase of the game (placement, movement, or endgame).
     */
    public void makeMove(Board board, int phase) {
        Random r = new Random();
        if (stonesToPlace == 9) {
            game.placePiece(r.nextInt(24));
            MillGameUI.incrementMinimaxMoves();
            log.log(Level.INFO, "Placed Random piece!");
        } else {
            if (phase == 1) {
                // Placement phase
                int bestPlacement = minimax.findBestPlacement(board, this);
                log.log(Level.INFO, "Best Placement {0}", bestPlacement);
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
                // Movement phase
                Node[] bestMove = minimax.findBestMove(board, this, phase);
                if (bestMove != null && bestMove[0] != null && bestMove[1] != null) {
                    try {
                        game.makeMove(bestMove[0].getId(), bestMove[1].getId());
                        MillGameUI.incrementMinimaxMoves();
                        log.log(Level.INFO, "AI moved from {0} to {1}", new Object[]{bestMove[0].getId(), bestMove[1].getId()});
                        // Check for mill formation
                        if (game.isMillFormed()) {
                            handleMillFormation(board);
                        }
                    } catch (InvalidMove e) {
                        log.log(Level.WARNING, "Failed to make move: {0}", e.getMessage());
                    }
                } else {
                    log.log(Level.WARNING, "No valid move found for AI.");
                }
            }
        }
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
    private void handleMillFormation(Board board) {
        Node bestRemovalNode = minimax.bestRemoval(board, this);
        if (bestRemovalNode != null) {
            try {
                game.removePiece(bestRemovalNode.getId());
                log.log(Level.INFO, "AI removed opponent's piece at node {0}", new Object[]{bestRemovalNode.getId()});
            } catch (InvalidMove e) {
                log.log(Level.WARNING, "Failed to remove piece: {0}", e.getMessage());
            }
        } else {
            log.log(Level.WARNING, "No opponent pieces to remove.");
        }
    }

}
