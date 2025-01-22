package minimax;

import game.mills.Board;
import game.mills.Node;
import game.mills.Player;
import game.mills.Game;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Arrays;

/**
 * The EvaluationFunction class provides scoring heuristics for different phases of the game
 * in order to evaluate the game state from the perspective of a given player.
 * It is used by the Minimax algorithm to determine the best moves based on board positions.
 */
@Setter
@Log
public class EvaluationFunction {

    private Game game;

    /**
     * A static set to keep track of visited board signatures, helping detect loops/repetitions.
     * If we re-encounter a board signature, we'll penalize it to discourage repetition.
     */
    private static final Set<String> visitedStates = new HashSet<>();

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
     * @param node   (Optional) The node involved in the most recent action (e.g., placement).
     * @return An integer score representing the board state from the player's perspective.
     */
    public int evaluate(Board board, Player player, int phase, Node node) {
        // -----------------------------
        // Simple Loop/Repetition Check
        // -----------------------------
        String signature = createBoardSignature(board);
        if (visitedStates.contains(signature)) {
            // Penalize repeated states to reduce loops. Adjust the penalty as desired.
            return -50;
        } else {
            visitedStates.add(signature);
        }
        // -----------------------------

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
     * @param node   The node most recently placed (if relevant).
     * @return A score based on piece placement quality, potential mills, and flexibility.
     */
    private int evaluatePlacementPhase(Board board, Player player, Node node) {
        int score = 0;

        if (node == null) {
            // If for some reason we have no node context, do a minimal eval.
            return score;
        }

        // Check for potential mills formed/threatened by the newly placed piece.
        long potentialMills = Arrays.stream(board.getMills())
                .parallel()
                .filter(mill -> Arrays.stream(mill).anyMatch(id -> id == node.getId()) &&
                        Arrays.stream(mill).allMatch(id -> 
                            board.getNode(id).getOccupant() == player 
                              || !board.getNode(id).isOccupied())
                )
                .count();
        score += (int) potentialMills * 20;

        // Check the opponent's potential mills (opponent has 2 stones + 1 empty in a mill).
        Player opponent = game.getOpponent(player);
        long potentialMillsOpponent = Arrays.stream(board.getMills())
                .parallel()
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

        // Check if we've blocked an opponent mill or formed one in the process.
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
                score += 40;  // Good block
            } else {
                score -= 100; // Opponent blocked us
            }
        }

        return score;
    }

    /**
     * Evaluates the board state during the movement phase.
     *
     * @param board  The game board.
     * @param player The player for whom the evaluation is performed.
     * @return A score based on mills, mobility, restricting opponent's movement,
     *         and potentially "opening" an existing mill to re-close it later.
     */
    private int evaluateMovementPhase(Board board, Player player) {
        int score = 0;

        // 1. Count how many full mills the player currently has.
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

        // 2. Mobility: how many moves are available to the current player?
        int mobility = 0;
        for (Node node : board.getNodes().values()) {
            if (node.getOccupant() == player) {
                for (Node neighbor : board.getNeighbours(node)) {
                    if (!neighbor.isOccupied()) {
                        mobility++;
                    }
                }
            }
        }
        score += mobility * 10;

        // 3. Opponent mobility: the fewer moves for opponent, the better for us.
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

        // 4. Reward "opening" an existing mill so it can be re-closed.
        score += rewardOpeningMill(board, player);

        return score;
    }

    /**
     * Evaluates the board state during the endgame phase.
     *
     * @param board  The game board.
     * @param player The player for whom the evaluation is performed.
     * @return A score based on piece count advantage, mills, and winning conditions.
     */
    private int evaluateEndgamePhase(Board board, Player player) {
        int score = 0;
        Player opponent = game.getOpponent(player);

        // Piece count difference
        int pieceCountDiff = countPieces(board, player) - countPieces(board, opponent);
        score += pieceCountDiff * 30;

        // Win/loss check
        // Opponent cannot move or has <=2 pieces => we are effectively winning
        if (!board.hasValidMoves(opponent) || countPieces(board, opponent) <= 2) {
            score += 500;
        }
        // If we cannot move or have <=2 pieces => losing
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

    // ------------------------------------------------------------------------
    // ADDITIONAL METHODS FOR LOOP CHECK & "OPEN A MILL" REWARD
    // ------------------------------------------------------------------------

    /**
     * Creates a simple board signature to detect repeated positions.
     * For a robust solution, consider using Zobrist hashing or a full transposition table.
     */
    private String createBoardSignature(Board board) {
        // Build a string occupant list in sorted node order: e.g., "P1--P2-P1-..."
        return board.getNodes().entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey)) // sort by node ID
                .map(e -> {
                    Node n = e.getValue();
                    if (!n.isOccupied()) return "-";
                    // Adjust to your Player's string representation, e.g. name or toString():
                    return n.getOccupant().toString();
                })
                .collect(Collectors.joining());
    }

    /**
     * Provides a small bonus for any piece in a completed mill that has at least one free neighbor,
     * suggesting the player could "lift" it out and re-place it for an immediate re-formed mill.
     */
    private int rewardOpeningMill(Board board, Player player) {
        int bonus = 0;

        // For each stone of the player, see if it's in a formed mill and can move.
        for (Node node : board.getNodes().values()) {
            if (node.getOccupant() == player) {
                // Check if node is part of a currently completed mill
                if (isPartOfCompleteMill(board, node.getId(), player)) {
                    // At least one adjacent empty node to "open" the mill
                    boolean hasEmptyNeighbor = board.getNeighbours(node)
                                                    .stream()
                                                    .anyMatch(n -> !n.isOccupied());
                    if (hasEmptyNeighbor) {
                        // Small reward for each piece that could "open" a mill
                        bonus += 15;
                    }
                }
            }
        }

        return bonus;
    }

    /**
     * Checks if a node is part of a completely formed mill (i.e., all 3 stones belong to the same player).
     */
    private boolean isPartOfCompleteMill(Board board, int nodeId, Player player) {
        for (int[] mill : board.getMills()) {
            // If this mill includes the node
            if (Arrays.stream(mill).anyMatch(id -> id == nodeId)) {
                // Check if all occupant are 'player'
                boolean allOwned = true;
                for (int id : mill) {
                    if (board.getNode(id).getOccupant() != player) {
                        allOwned = false;
                        break;
                    }
                }
                if (allOwned) {
                    return true;
                }
            }
        }
        return false;
    }
}
