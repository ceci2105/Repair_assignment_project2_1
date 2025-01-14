package AlphaBetaPruning;


import java.util.logging.Level;

import Minimax.MinimaxAIPlayer;
import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import lombok.extern.java.Log;

/**
 * The AlphaBetaAIPlayer class represents an AI-controlled player that uses the Alpha-Beta algorithm
 * to make strategic moves in the game. It extends the Player interface to interact with the game,
 * and calculates moves based on the current board state, game phase, and opponent's position.
 */
@Log
public class AlphaBetaAIPlayer extends MinimaxAIPlayer {

    private final AlphaBetaAlgorithm alphaBeta; // Instance of AlphaBetaAlgorithm for calculating the best moves

    /**
     * Constructor to initialize the AlphaBetaAIPlayer with a given name, depth, game, and color.
     *
     * @param name  The name of the AI player.
     * @param depth The search depth for the Alpha-Beta algorithm.
     * @param game  The game instance for accessing board and opponent information.
     * @param color The color representing the AI player’s pieces on the board.
     */
    public AlphaBetaAIPlayer(String name, Color color, Game game, int depth) {
        super(name, color, game, depth);
        this.alphaBeta = new AlphaBetaAlgorithm(game, depth); // Initialize the Alpha-Beta algorithm
    }

    @Override
    public void makeMove(Board board, int phase) {
        Platform.runLater(() -> {
            if (getStonesToPlace() > 0 && phase == 1) {
                int bestPlacement = alphaBeta.findBestPlacement(board, this);
                if (bestPlacement != -1) {
                    try {
                        game.placePiece(bestPlacement);
                        decrementStonesToPlace();
                        incrementStonesOnBoard();
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Failed to place piece: {0}", e.getMessage());
                    }
                }
            } else {
                Node[] bestMove = alphaBeta.findBestMove(board, this, phase);
                if (bestMove != null && bestMove[0] != null && bestMove[1] != null) {
                    try {
                        game.makeMove(bestMove[0].getId(), bestMove[1].getId());
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Failed to make move: {0}", e.getMessage());
                    }
                }
            }
        });
    }
}
