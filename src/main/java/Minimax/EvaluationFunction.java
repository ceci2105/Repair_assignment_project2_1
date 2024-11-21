package Minimax;

import game.mills.Board;
import game.mills.Node;
import game.mills.Player;
import game.mills.Game;
import lombok.extern.java.Log;

import java.util.List;
import java.util.logging.Level;

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
     * @return A score based on piece placement quality, potential mills, and flexibility.
     */
    private int evaluatePlacementPhase(Board board, Player player, Node node) {
        int score = 0;

        //List<Node> neighbours = board.getNeighbours(node);
        //for (Node nodes : neighbours) {
            if (node.getOccupant() == player) {
                score += 5; // Basic score for each placed piece

                // Additional score if the piece forms a mill
                if (board.checkMill(node, player)) {
                    score += 100;
                }

                // Reward flexibility in placement by scoring based on the number of neighboring nodes
                score += board.getPlayerNeighbours(node.getId(), player) * 10;
            }
        // }
        log.log(Level.INFO, "Score: {0}", score);
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

        // Evaluate based on mills and potential mobility
        for (Node node : board.getNodes().values()) {
            if (node.getOccupant() == player) {
                if (board.checkMill(node, player)) {
                    score += 20; // Reward forming mills
                }
                // Reward mobility based on unoccupied neighboring nodes
                for (Node neighbor : board.getNeighbours(node)) {
                    if (!neighbor.isOccupied()) {
                        score += 5;
                    }
                }
            }
        }

        // Additional score if the opponent has no valid moves, giving player a strategic advantage
        if (!board.hasValidMoves(opponent)) {
            score += 50; // High reward if opponent has no valid moves
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
        int score = 0;
        Player opponent = game.getOpponent(player);

        // Check for winning or losing conditions
        if (!board.hasValidMoves(opponent) || countPieces(board, opponent) < 3) {
            return Integer.MAX_VALUE; // Winning condition for the player
        } else if (!board.hasValidMoves(player) || countPieces(board, player) < 3) {
            return Integer.MIN_VALUE; // Losing condition for the player
        }

        // Reward mills and compare piece counts between the player and the opponent
        for (Node node : board.getNodes().values()) {
            if (node.getOccupant() == player && board.checkMill(node, player)) {
                score += 30; // High score for each mill
            }
        }

        // Additional score based on piece count advantage
        score += (countPieces(board, player) - countPieces(board, opponent)) * 15;
        return score;
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
            if (node.getOccupant() == player) {
                count++;
            }
        }
        return count;
    }
}