package Minimax;

import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
import lombok.extern.java.Log;

/**
 * The EvaluationFunction class provides scoring heuristics for different phases of the game
 * in order to evaluate the game state from the perspective of a given player.
 * It is used by the Minimax algorithm to determine the best moves based on board positions.
 */
@Log
public class EvaluationFunction {

    private Game game;

    /**
     * Constructor to initialize the EvaluationFunction with a Game instance.
     * @param game The current game instance, used to access information such as the opponent.
     */
    public EvaluationFunction(Game game) {
        this.game = game;
    }

    /**
     * Evaluates the board state based on the current game phase.
     * @param board The game board.
     * @param player The player for whom the evaluation is performed.
     * @param phase The current game phase (1 - placement, 2 - movement, 3 - endgame).
     * @return An integer score representing the board state from the player's perspective.
     */
    public int evaluate(Board board, Player player, int phase, Node node) {
        switch (phase) {
            case 1:
                return evaluatePlacementPhase(board, player, node);
            case 2:
                return evaluateMovementPhase(board, player);
            case 3:
                return evaluateEndgamePhase(board, player);
            default:
                return 0;
        }
    }

    /**
     * Evaluates the board state during the placement phase.
     * @param board The game board.
     * @param player The player for whom the evaluation is performed.
     * @param node The node to evaluate.
     * @return A score based on piece placement quality, potential mills, and flexibility.
     */
    private int evaluatePlacementPhase(Board board, Player player, Node node) {
        int score = 0;
        
        // Null check for node
        if (node != null && node.getOccupant() == player) {
            score += 5;

            // Check if the node forms a mill
            if (board.checkMill(node, player)) {
                score += 100;
            }

            // Check the number of player neighbors
            score += board.getPlayerNeighbours(node.getId(), player) * 10;

            // Check if the node will form a mill with opponent's pieces
            if (board.willFormMill(node, game.getOpponent(player), board)) {
                score = 200;
            }
        }

        return score;
    }

    /**
     * Evaluates the board state during the movement phase.
     * @param board The game board.
     * @param player The player for whom the evaluation is performed.
     * @return A score based on mills, mobility, and restricting opponent's movement.
     */
    private int evaluateMovementPhase(Board board, Player player) {
        int score = 0;
        Player opponent = game.getOpponent(player);

        for (Node node : board.getNodes().values()) {
            if (node != null && node.getOccupant() == player) {  // Null check for nodes
                if (board.checkMill(node, player)) {
                    score += 20;
                }
                for (Node neighbor : board.getNeighbours(node)) {
                    if (neighbor != null && !neighbor.isOccupied()) {  // Null check for neighbors
                        score += 5;
                    }
                }
            }
        }
        
        if (!board.hasValidMoves(opponent)) {
            score += 50;
        }

        return score;
    }

    /**
     * Evaluates the board state during the endgame phase.
     * @param board The game board.
     * @param player The player for whom the evaluation is performed.
     * @return A score based on mills, piece count advantage, and winning conditions.
     */
    private int evaluateEndgamePhase(Board board, Player player) {
        return 0;
    }

    /**
     * Counts the number of pieces a player has on the board.
     * @param board The game board.
     * @param player The player whose pieces are to be counted.
     * @return The count of pieces belonging to the player.
     */
    private int countPieces(Board board, Player player) {
        int count = 0;
        for (Node node : board.getNodes().values()) {
            if (node != null && node.getOccupant() == player) {  // Null check for nodes
                count++;
            }
        }
        return count;
    }
}
