// Implements Alpha-Beta pruning for optimal move computation

package AlphaBetaPruning;

import Minimax.EvaluationFunction;
import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
import lombok.extern.java.Log;

/**
 * The AlphaBetaAlgorithm class implements the Alpha-Beta pruning technique
 * to determine the best move for the AI player. It optimizes the Minimax algorithm
 * by pruning branches that cannot influence the final decision.
 */
@Log
public class AlphaBetaAlgorithm {
    private final int depth;
    private final EvaluationFunction evaluationFunction;
    private final Game game;

    /**
     * Constructor to initialize AlphaBetaAlgorithm with a Game instance and search depth.
     * @param game The current game instance.
     * @param depth The maximum search depth for the Alpha-Beta algorithm.
     */
    public AlphaBetaAlgorithm(Game game, int depth) {
        this.game = game;
        this.depth = depth;
        this.evaluationFunction = new EvaluationFunction(game);
    }

    /**
     * Finds the best placement for the given player during the placement phase.
     * @param board The current game board.
     * @param player The player for whom the best placement is being calculated.
     * @return The ID of the best placement node.
     */
    public int findBestPlacement(Board board, Player player) {
        int alpha = Integer.MIN_VALUE; // Initial value for alpha
        int beta = Integer.MAX_VALUE;  // Initial value for beta
        int bestPlacement = -1;

        for (Node node : board.getNodes().values()) {
            if (!node.isOccupied()) {
                board.placePieceAgent(player, node.getId());
                int eval = alphaBeta(board, depth - 1, alpha, beta, false, player, 1);
                board.removePieceAgent(node.getId()); // Undo the placement
                
                if (eval > alpha) {
                    alpha = eval;
                    bestPlacement = node.getId();
                }
            }
        }
        return bestPlacement;
    }

    /**
     * Finds the best move for the given player using Alpha-Beta pruning.
     * @param board The current game board.
     * @param player The player for whom the best move is being calculated.
     * @param phase The current phase of the game.
     * @return An array containing the best fromNode and toNode representing the move.
     */
    public Node[] findBestMove(Board board, Player player, int phase) {
        Node[] bestMove = new Node[2];
        int bestValue = Integer.MIN_VALUE;

        for (Node fromNode : board.getNodes().values()) {
            if (fromNode.getOccupant() == player) {
                for (Node toNode : board.getNeighbours(fromNode)) {
                    if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                        board.movePiece(player, fromNode.getId(), toNode.getId());
                        int moveValue = alphaBeta(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, player, phase);
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
        return bestMove;
    }

    /**
     * Alpha-Beta pruning algorithm.
     * @param board The game board.
     * @param depth The depth of the search tree.
     * @param alpha The best value that the maximizer can guarantee.
     * @param beta The best value that the minimizer can guarantee.
     * @param isMaximizingPlayer Whether the current move is for the maximizing player.
     * @param player The player making the move.
     * @param phase The current game phase.
     * @return The evaluation score of the board.
     */
    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer, Player player, int phase) {
        if (depth == 0 || game.isGameOver()) {
            return evaluationFunction.evaluate(board, player, phase, null);
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Node fromNode : board.getNodes().values()) {
                if (fromNode.getOccupant() == player) {
                    for (Node toNode : board.getNeighbours(fromNode)) {
                        if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                            board.movePiece(player, fromNode.getId(), toNode.getId());
                            int eval = alphaBeta(board, depth - 1, alpha, beta, false, player, phase);
                            board.movePiece(player, toNode.getId(), fromNode.getId());
                            maxEval = Math.max(maxEval, eval);
                            alpha = Math.max(alpha, eval);
                            if (beta <= alpha) {
                                return maxEval; // Prune branch
                            }
                        }
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            Player opponent = game.getOpponent(player);
            for (Node fromNode : board.getNodes().values()) {
                if (fromNode.getOccupant() == opponent) {
                    for (Node toNode : board.getNeighbours(fromNode)) {
                        if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                            board.movePiece(opponent, fromNode.getId(), toNode.getId());
                            int eval = alphaBeta(board, depth - 1, alpha, beta, true, player, phase);
                            board.movePiece(opponent, toNode.getId(), fromNode.getId());
                            minEval = Math.min(minEval, eval);
                            beta = Math.min(beta, eval);
                            if (beta <= alpha) {
                                return minEval; // Prune branch
                            }
                        }
                    }
                }
            }
            return minEval;
        }
    }
}