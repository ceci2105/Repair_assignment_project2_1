package minimax;

import game.mills.Board;
import game.mills.Node;
import game.mills.Player;
import game.mills.Game;
import lombok.Setter;
import lombok.extern.java.Log;


import java.util.Arrays;
import java.util.Objects;

/**
 * The EvaluationFunction class provides scoring heuristics for different phases of the game
 * in order to evaluate the game state from the perspective of a given player.
 * It is used by the Minimax algorithm to determine the best moves based on board positions.
 */
@Log
public class EvaluationFunction {
    @Setter
    private Game game;

    /**
     * Constructor to initialize the EvaluationFunction with a Game instance.
     */
    public EvaluationFunction(Game game) {
        this.game = game;

    }

    /**
     * Evaluates the board state based on the current game phase.
     *
     * @param board  The game board.
     * @param player The player for whom the evaluation is performed.
     * @param phase  The current game phase (1 - placement, 2 - movement, 3 - endgame).
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
     *
     * @param board  The game board.
     * @param player The player for whom the evaluation is performed.
     * @return A score based on piece placement quality, potential mills, and flexibility.
     */
    private int evaluatePlacementPhase(Board board, Player player, Node node) {
        int score = 0;
        // Here we check for potential mills for the method calling player.
        long potentialMills = Arrays.stream(board.getMills()).parallel()
                .filter(mill -> Arrays.stream(mill).anyMatch(id -> id == node.getId()) &&
                        Arrays.stream(mill).allMatch(id -> board.getNode(id).getOccupant() == player || !board.getNode(id).isOccupied()))
                .count();
        score += (int) potentialMills * 20;

        // Here we check the opponents potential mills
        Player opponent = game.getOpponent(player);
        long potentialMillsOpponent = Arrays.stream(board.getMills()).parallel()
                .filter(mill -> {
                    long opponentStones = Arrays.stream(mill)
                            .filter(id -> board.getNode(id).getOccupant() == opponent)
                            .count();
                    long emptySpots = Arrays.stream(mill)
                            .filter(id -> !board.getNode(id).isOccupied())
                            .count();
                    return opponentStones == 2 && emptySpots == 1;
                })
                .count();
        score -= (int) (potentialMillsOpponent * 100);

        // Boolean condition to check, wether we block a mill or not.
        boolean blockedMill = Arrays.stream(board.getMills()).parallel()
                .anyMatch(mill -> Arrays.stream(mill).anyMatch(id -> id == node.getId()) &&
                        Arrays.stream(mill)
                                .filter(id -> id != node.getId())
                                .mapToObj(board::getNode)
                                .map(Node::getOccupant)
                                .filter(Objects::nonNull)
                                .distinct()
                                .count() == 1);

        if (blockedMill) {
            if (node.getOccupant() == player) {
                score += 40;
            } else {
                score -= 100;
            }
        }
        return score;
    }

    /**
     * Evaluates the board state during the movement phase.
     *
     * @param board  The game board.
     * @param player The player for whom the evaluation is performed.
     * @return A score based on mills, mobility, and restricting opponent's movement.
     */
    private int evaluateMovementPhase(Board board, Player player) {
        int score = 0;

        if (player instanceof MinimaxAIPlayer && ((MinimaxAIPlayer) player).isRepeatedMove(board)) {
            score -= 700;
        }

        int numMills = 0;
        for (int[] mill : board.getMills()) {
                boolean isMill = true;
                for (int nodeID : mill) {
                    if (board.getNode(nodeID).getOccupant() != player) {
                        isMill = false;
                        break;
                    }
                }
                if (isMill) {
                    numMills++;
                }
            }


        score += numMills * 50;

        int mobility = 0;
        for (Node node : board.getNodes().values()) {
            if (node.getOccupant() == player) {
                for (Node neighbuor : board.getNeighbours(node)) {
                    if (!neighbuor.isOccupied()) {
                        mobility++;
                    }
                }
            }
        }

        score += mobility * 10;

        Player opponent = game.getOpponent(player);
        int opponentMobility = 0;
        for (Node node : board.getNodes().values()) {
            if (node.getOccupant() == opponent) {
                for (Node neighbour : board.getNeighbours(node)) {
                    if (!neighbour.isOccupied()) {
                        opponentMobility++;
                    }
                }
            }
        }
        score -= opponentMobility * 10;
        return score;
    }

    /**
     * Evaluates the board state during the endgame phase.
     *
     * @param board  The game board.
     * @param player The player for whom the evaluation is performed.
     * @return A score based on mills, piece count advantage, and winning conditions.
     */
    private int evaluateEndgamePhase(Board board, Player player) {
        int score = 0;
        Player opponent = game.getOpponent(player);

        int pieceCountDiff = countPieces(board, player) - countPieces(board, opponent);
        score += pieceCountDiff * 30;

        if (!board.hasValidMoves(opponent) || countPieces(board, opponent) <= 2) {
            score += 500;
        }
        if (!board.hasValidMoves(player) || countPieces(board, player) <= 2) {
            score -= 500;
        }
        return score;
    }

    /**
     * Counts the number of pieces a player has on the board.
     *
     * @param board  The game board.
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