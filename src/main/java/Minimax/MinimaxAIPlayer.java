package Minimax;

import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
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
    private int depth;                   // The search depth for the Minimax algorithm
    private MinimaxAlgorithm minimax;    // Instance of MinimaxAlgorithm for calculating the best moves
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
            log.log(Level.INFO, "Placed Random piece!");
        } else {
            if (phase == 1) {  // If in placement phase, place a stone
                int bestPlacement = minimax.findBestPlacement(board, this);
                int i = 1;
                if (bestPlacement != -1) {
                    game.placePiece(bestPlacement);
                }
            } else {  // Else proceed with regular movement
                Node[] bestMove = minimax.findBestMove(board, this, phase);
                if (bestMove != null && bestMove[0] != null && bestMove[1] != null) {
                    board.movePiece(this, bestMove[0].getId(), bestMove[1].getId());
                    System.out.println("AI moved a stone from node " + bestMove[0].getId() + " to node " + bestMove[1].getId());
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

}
