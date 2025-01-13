package AlphaBetaPruning;

import java.util.Random;
import java.util.logging.Level;

import game.mills.Board;
import game.mills.Game;
import game.mills.InvalidMove;
import game.mills.Node;
import game.mills.Player;
import gui.MillGameUI;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

/**
 * The AlphaBetaAIPlayer class represents an AI-controlled player that uses the Alpha-Beta algorithm
 * to make strategic moves in the game. It extends the Player interface to interact with the game,
 * and calculates moves based on the current board state, game phase, and opponent's position.
 */
@Log
public class AlphaBetaAIPlayer implements Player {
    private final int depth;                   // The search depth for the AlphaBeta algorithm
    private final AlphaBetaAlgorithm alphaBeta;    // Instance of AlphaBetaAlgorithm for calculating the best moves
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
     * Constructor to initialize the AlphaBetaAIPlayer with a given name, depth, game, and color.
     *
     * @param name  The name of the AI player.
     * @param depth The search depth for the AlphaBeta algorithm.
     * @param game  The game instance for accessing board and opponent information.
     * @param color The color representing the AI player’s pieces on the board.
     */
    public AlphaBetaAIPlayer(String name, Color color, Game game, int depth) {
        this.name = name;
        this.color = color;
        this.depth = depth;
        this.game = game;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
        this.alphaBeta = new AlphaBetaAlgorithm(game, depth); // Initialize the AlphaBeta algorithm
    }

    /**
     * Executes the best move calculated by the AlphaBeta algorithm for the given board and phase.
     * The method finds the optimal move using AlphaBeta and applies it to the board.
     *
     * @param board The game board on which the move is to be made.
     * @param phase The current phase of the game (placement, movement, or endgame).
     */
    public void makeMove(Board board, int phase) {
        Platform.runLater(() -> {
            if (stonesToPlace == 9) {
                Random r = new Random();
                int randomPlacement = r.nextInt(24);
                if (board.getNode(randomPlacement).isOccupied()) {
                    randomPlacement = r.nextInt(24);
                    game.placePiece(randomPlacement);
                    MillGameUI.incrementAlphaBetaMoves();
                } else {
                    game.placePiece(randomPlacement);
                    MillGameUI.incrementAlphaBetaMoves();
                }
            } else {
                if (phase == 1) {
                    // Placement phase
                    int bestPlacement = alphaBeta.findBestPlacement(board, this);
                    if (bestPlacement != -1) {
                        try {
                            game.placePiece(bestPlacement);
                            MillGameUI.incrementAlphaBetaMoves();
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
                    Node[] bestMove = alphaBeta.findBestMove(board, this, phase);
                    if (bestMove != null && bestMove[0] != null && bestMove[1] != null) {
                        try {
                            game.makeMove(bestMove[0].getId(), bestMove[1].getId());
                            MillGameUI.incrementAlphaBetaMoves();
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
    private void handleMillFormation(Board board) {
        Node bestRemovalNode = alphaBeta.bestRemoval(board, this);
        if (bestRemovalNode != null) {
            try {
                game.removePiece(bestRemovalNode.getId());
            } catch (InvalidMove e) {
                log.log(Level.WARNING, "Failed to remove piece: {0}", e.getMessage());
            }
        } else {
            log.log(Level.WARNING, "No opponent pieces to remove.");
        }
    }
}
