package AlphaBetaPruning;

import Minimax.MinimaxAlgorithm;
import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
import lombok.extern.java.Log;

/**
 * The AlphaBetaAlgorithm class implements the Alpha-Beta pruning optimization for the Minimax algorithm.
 * It reduces the number of nodes evaluated by the Minimax algorithm by pruning branches that cannot affect the final decision.
 */
@Log
public class AlphaBetaAlgorithm extends MinimaxAlgorithm {

    /**
     * Constructor to initialize AlphaBetaAlgorithm with a Game instance and search depth.
     *
     * @param game  The current game instance, used to access board and player information.
     * @param depth The maximum search depth for the Alpha-Beta algorithm.
     */
    public AlphaBetaAlgorithm(Game game, int depth) {
        super(game, depth);
    }

    /**
     * Alpha-Beta pruning algorithm to evaluate board states up to a certain depth.
     * Alternates between maximizing and minimizing to simulate both players' moves.
     *
     * @param board                The current game board.
     * @param depth                The remaining depth for the recursive search.
     * @param alpha                The best already explored option along the path to the root for the maximizer.
     * @param beta                 The best already explored option along the path to the root for the minimizer.
     * @param isMaximizingPlayer   True if the current move is for the maximizing player, false for minimizing.
     * @param player               The player for whom the best score is being calculated.
     * @param phase                The current phase of the game.
     * @return An integer score representing the board evaluation at this depth.
     */
    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer, Player player, int phase) {
        System.out.println("AlphaBeta call - Depth: " + depth + ", Alpha: " + alpha + ", Beta: " + beta + ", Maximizing: " + isMaximizingPlayer);

        if (depth == 0 || game.isGameOver) {
            int evaluation = evaluationFunction.evaluate(board, player, phase, null);
            System.out.println("Leaf Node Reached: Evaluation: " + evaluation);
            return evaluation;
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Node fromNode : board.getNodes().values()) {
                if (fromNode.getOccupant() == player) {
                    for (Node toNode : board.getNeighbours(fromNode)) {
                        if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                            board.movePiece(player, fromNode.getId(), toNode.getId());
                            System.out.println("Maximizer - Moved from " + fromNode.getId() + " to " + toNode.getId());

                            if (board.checkMill(toNode, player)) {
                                for (Node opponentNode : board.getNodes().values()) {
                                    if (opponentNode.getOccupant() == game.getOpponent(player) && !board.isPartOfMill(opponentNode)) {
                                        board.removePieceAgent(opponentNode.getId());
                                        System.out.println("Maximizer - Removed opponent piece at " + opponentNode.getId());
                                        int eval = alphaBeta(board, depth - 1, alpha, beta, false, player, phase);
                                        board.getNode(opponentNode.getId()).setOccupant(game.getOpponent(player)); // Undo remove
                                        maxEval = Math.max(maxEval, eval);
                                        alpha = Math.max(alpha, eval);
                                        if (beta <= alpha) {
                                            System.out.println("Beta cutoff at depth: " + depth);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                int eval = alphaBeta(board, depth - 1, alpha, beta, false, player, phase);
                                maxEval = Math.max(maxEval, eval);
                                alpha = Math.max(alpha, eval);
                            }

                            board.movePiece(player, toNode.getId(), fromNode.getId()); // Undo move
                            if (beta <= alpha) {
                                break;
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
                            System.out.println("Minimizer - Moved from " + fromNode.getId() + " to " + toNode.getId());

                            if (board.checkMill(toNode, opponent)) {
                                for (Node playerNode : board.getNodes().values()) {
                                    if (playerNode.getOccupant() == player && !board.isPartOfMill(playerNode)) {
                                        board.removePieceAgent(playerNode.getId());
                                        System.out.println("Minimizer - Removed player piece at " + playerNode.getId());
                                        int eval = alphaBeta(board, depth - 1, alpha, beta, true, player, phase);
                                        board.getNode(playerNode.getId()).setOccupant(player); // Undo remove
                                        minEval = Math.min(minEval, eval);
                                        beta = Math.min(beta, eval);
                                        if (beta <= alpha) {
                                            System.out.println("Alpha cutoff at depth: " + depth);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                int eval = alphaBeta(board, depth - 1, alpha, beta, true, player, phase);
                                minEval = Math.min(minEval, eval);
                                beta = Math.min(beta, eval);
                            }

                            board.movePiece(opponent, toNode.getId(), fromNode.getId()); // Undo move
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

    @Override
    public Node[] findBestMove(Board board, Player player, int phase) {
        Node[] bestMove = new Node[2];
        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        System.out.println("Finding best move for player: " + player + " in phase: " + phase);

        for (Node fromNode : board.getNodes().values()) {
            if (fromNode.getOccupant() == player) {
                for (Node toNode : board.getNeighbours(fromNode)) {
                    if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                        board.movePiece(player, fromNode.getId(), toNode.getId());
                        System.out.println("Testing move from " + fromNode.getId() + " to " + toNode.getId());

                        int moveValue = alphaBeta(board, depth - 1, alpha, beta, false, player, phase);
                        if (moveValue > bestValue) {
                            bestValue = moveValue;
                            bestMove[0] = fromNode;
                            bestMove[1] = toNode;
                        }
                        alpha = Math.max(alpha, moveValue);

                        board.movePiece(player, toNode.getId(), fromNode.getId()); // Undo move
                    }
                }
            }
        }

        if (bestMove[0] != null) {
            System.out.println("Best move found: From " + bestMove[0].getId() + " to " + bestMove[1].getId() + " with value: " + bestValue);
        } else {
            System.out.println("Best move is null, no valid move found.");
        }
        return bestMove;
    }
}
