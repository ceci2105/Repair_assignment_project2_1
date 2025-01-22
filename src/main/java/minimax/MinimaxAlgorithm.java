package minimax;

import game.mills.Board;
import game.mills.Game;
import game.mills.Player;
import game.mills.Node;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The MinimaxAlgorithm class implements the Minimax algorithm to determine the best move for the AI player.
 * It evaluates potential moves up to a given depth and chooses the move with the optimal outcome for the player.
 * This class includes both the primary Minimax algorithm with recursive depth-limited search and evaluation.
 */
@Log
public class MinimaxAlgorithm {
    private final int depth;
    private EvaluationFunction evaluationFunction;
    @Setter
    private Game game;

    /**
     * Constructor to initialize MinimaxAlgorithm with a Game instance and search depth.
     *
     * @param depth                The maximum search depth for the Minimax algorithm.
     * @param evaluationFunction   The evaluation function used to score board states.
     * @param game                 The current game instance.
     */
    public MinimaxAlgorithm(int depth, EvaluationFunction evaluationFunction, Game game) {
        this.depth = depth;
        this.game = game;
        // Although an evaluationFunction is passed in, the original code re-initialized it.
        // To respect "without changing previous implementations," we keep that behavior:
        this.evaluationFunction = new EvaluationFunction(game); 
        // If you prefer using the passed-in EF, comment out above and uncomment below:
        // this.evaluationFunction = evaluationFunction;
    }

    /**
     * Finds the best placement (node ID) for the given player by checking all empty spots 
     * and picking the one with the highest evaluation score (placement phase).
     */
    public int findBestPlacement(Board board, Player player) {
        int bestValue = Integer.MIN_VALUE;
        int bestPlacement = -1;
        Board copyBoard = board.deepCopy();

        for (Node node : copyBoard.getNodes().values()) {
            if (!node.isOccupied()) {
                copyBoard.placePieceAgent(player, node.getId());
                int placementValue = evaluationFunction.evaluate(copyBoard, player, 1, node);
                if (placementValue > bestValue) {
                    bestValue = placementValue;
                    bestPlacement = node.getId();
                }
                // Undo the temporary placement
                Node tempNode = copyBoard.getNode(node.getId());
                tempNode.setOccupant(null);
            }
        }
        // log.log(Level.INFO, "Best placement {0}", bestPlacement);
        return bestPlacement;
    }

    /**
     * Finds the best move (fromNode -> toNode) for the given player using the Minimax algorithm.
     * @param board  The current game board.
     * @param player The AI player for whom the best move is being calculated.
     * @param phase  The current phase of the game (1 = placement, 2 = movement, 3 = endgame).
     * @return An array containing [fromNode, toNode] representing the best move.
     */
    public Node[] findBestMove(Board board, Player player, int phase) {
        Node[] bestMove = new Node[2];
        int bestValue = Integer.MIN_VALUE;

        // Try all possible moves from every node occupied by 'player'.
        for (Node fromNode : board.getNodes().values()) {
            if (fromNode.getOccupant() == player) {
                for (Node toNode : board.getNeighbours(fromNode)) {
                    if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                        // Make the move (temporarily)
                        board.movePiece(player, fromNode.getId(), toNode.getId());
                        // Evaluate via minimax
                        int moveValue = minimax(board, depth - 1, false, player, phase);
                        // Undo move
                        board.movePiece(player, toNode.getId(), fromNode.getId());

                        if (moveValue > bestValue) {
                            bestValue = moveValue;
                            bestMove[0] = fromNode;
                            bestMove[1] = toNode;
                        }
                    }
                }
            }
        }
        // log.log(Level.INFO, "Best Value: {0}", bestValue);
        return bestMove;
    }

    // ------------------------------------------------------------------------------------
    // Minimax with alpha-beta
    // ------------------------------------------------------------------------------------

    private int minimax(Board board, int depth, boolean isMaximizingPlayer, Player player, int phase) {
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        return minimax(board, depth, isMaximizingPlayer, player, phase, alpha, beta);
    }

    /**
     * Recursive Minimax function (with alpha-beta pruning) to evaluate board states up to a certain depth.
     * 
     * @param board              The current game board.
     * @param depth              The remaining depth for the recursive search.
     * @param isMaximizingPlayer True if the current move is for the maximizing player, false otherwise.
     * @param player             The player (AI) for whom the best score is being calculated.
     * @param phase              The current phase of the game.
     * @param alpha              Alpha value for alpha-beta pruning.
     * @param beta               Beta value for alpha-beta pruning.
     * @return An integer score representing the evaluated board state at this depth.
     */
    private int minimax(Board board, int depth, boolean isMaximizingPlayer, Player player, int phase,
                        int alpha, int beta) {

        // If we've reached max depth or the game is over (no valid moves, etc.)
        if (depth == 0 || game.isGameOver) {
            return evaluationFunction.evaluate(board, player, phase, null);
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;

            // Try all moves for the AI (player)
            for (Node fromNode : board.getNodes().values()) {
                if (fromNode.getOccupant() == player) {
                    for (Node toNode : board.getNeighbours(fromNode)) {
                        if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                            // Make move
                            board.movePiece(player, fromNode.getId(), toNode.getId());
                            int eval = minimax(board, depth - 1, false, player, phase, alpha, beta);
                            // Undo move
                            board.movePiece(player, toNode.getId(), fromNode.getId());

                            maxEval = Math.max(maxEval, eval);
                            alpha = Math.max(alpha, eval);
                            if (beta <= alpha) {
                                break;
                            }
                        }
                    }
                }
            }
            return maxEval;

        } else {
            // Minimizing player (opponent)
            int minEval = Integer.MAX_VALUE;
            Player opponent = game.getOpponent(player);

            // Try all moves for the opponent
            for (Node fromNode : board.getNodes().values()) {
                if (fromNode.getOccupant() == opponent) {
                    for (Node toNode : board.getNeighbours(fromNode)) {
                        if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                            // Make move
                            board.movePiece(opponent, fromNode.getId(), toNode.getId());
                            int eval = minimax(board, depth - 1, true, player, phase, alpha, beta);
                            // Undo move
                            board.movePiece(opponent, toNode.getId(), fromNode.getId());

                            minEval = Math.min(minEval, eval);
                            beta = Math.min(beta, eval);
                            if (beta <= alpha) {
                                break;
                            }
                        }
                    }
                }
            }
            return minEval;
        }
    }

    // ------------------------------------------------------------------------------------
    // Removing an opponent piece after forming a mill
    // ------------------------------------------------------------------------------------

    /**
     * Returns the best node to remove from the opponent once a mill is formed.
     * In this basic version, we remove a random opponent stone that is not in a mill if possible;
     * otherwise, remove any of the opponent's stones.
     */
    public Node bestRemoval(Board board, Player player) {
        Player opponent = game.getOpponent(player);
        List<Node> removableNodes = new ArrayList<>();

        // First, collect opponent's stones that are NOT in mills
        for (Node node : board.getNodes().values()) {
            if (node.isOccupied() && node.getOccupant() == opponent && !board.isPartOfMill(node)) {
                removableNodes.add(node);
            }
        }

        if (removableNodes.isEmpty()) {
            // All opponent's stones are in mills => can remove any
            for (Node node : board.getNodes().values()) {
                if (node.isOccupied() && node.getOccupant() == opponent) {
                    removableNodes.add(node);
                }
            }
        }

        if (removableNodes.isEmpty()) {
            // No opponent stones to remove
            return null;
        }

        // Pick one randomly from the valid list
        Random random = new Random();
        int index = random.nextInt(removableNodes.size());
        return removableNodes.get(index);
    }
}
