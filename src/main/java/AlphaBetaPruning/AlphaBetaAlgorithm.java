package AlphaBetaPruning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Minimax.EvaluationFunction;
import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
import lombok.extern.java.Log;

/**
 * The AlphaBetaAlgorithm class implements the Alpha-Beta pruning algorithm to determine the best move for the AI player.
 * It evaluates potential moves up to a given depth and chooses the move with the optimal outcome for the player.
 * This class includes both the Alpha-Beta pruning algorithm with recursive depth-limited search and evaluation function.
 */
@Log
public class AlphaBetaAlgorithm {
    private final int depth;
    private EvaluationFunction evaluationFunction;
    private Game game;

    /**
     * Constructor to initialize AlphaBetaAlgorithm with a Game instance and search depth.
     * @param game The current game instance, used to access board and player information.
     * @param depth The maximum search depth for the Alpha-Beta pruning algorithm.
     */
    public AlphaBetaAlgorithm(Game game, int depth) {
        this.game = game;
        this.depth = depth;
        this.evaluationFunction = new EvaluationFunction(game);
    }

    /**
     * Finds the best placement for the AI player during the placement phase using Alpha-Beta pruning.
     * @param board The current game board.
     * @param player The AI player.
     * @return The best placement node ID.
     */
    public int findBestPlacement(Board board, Player player) {
        int bestValue = Integer.MIN_VALUE;
        int bestPlacement = -1;
        Board copyBoard = board.deepCopy();

        for (Node node : copyBoard.getNodes().values()) {
            if (!node.isOccupied()) {
                copyBoard.placePieceAgent(player, node.getId());
                int placementValue = evaluationFunction.evaluate(copyBoard, player, 1, node); // Evaluate
                if (placementValue > bestValue) {
                    bestValue = placementValue;
                    bestPlacement = node.getId();
                }

                Node tempNode = copyBoard.getNode(node.getId());
                tempNode.setOccupant(null);
            }
        }

        return bestPlacement;
    }

    /**
     * Finds the best move for the given player using the Alpha-Beta pruning algorithm.
     * @param board The current game board.
     * @param player The player for whom the best move is being calculated.
     * @param phase The current phase of the game (placement, movement, or endgame).
     * @return An array containing the best fromNode and toNode representing the move.
     */
    public Node[] findBestMove(Board board, Player player, int phase) {
        Node[] bestMove = new Node[2];
        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Node fromNode : board.getNodes().values()) {
            if (fromNode.getOccupant() == player) {
                for (Node toNode : board.getNeighbours(fromNode)) {
                    if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                        board.movePiece(player, fromNode.getId(), toNode.getId());
                        int moveValue = alphaBeta(board, depth - 1, alpha, beta, false, player, phase);
                        board.movePiece(player, toNode.getId(), fromNode.getId());

                        if (moveValue > bestValue) {
                            bestValue = moveValue;
                            bestMove[0] = fromNode;
                            bestMove[1] = toNode;
                        }
                        alpha = Math.max(alpha, bestValue);
                    }
                }
            }
        }

        return bestMove;
    }

    /**
     * Recursive Alpha-Beta function to evaluate board states up to a certain depth.
     * Alternates between maximizing and minimizing to simulate both players' moves.
     * @param board The current game board.
     * @param depth The remaining depth for the recursive search.
     * @param alpha The best value that the maximizing player can guarantee so far.
     * @param beta The best value that the minimizing player can guarantee so far.
     * @param isMaximizingPlayer True if the current move is for the maximizing player, false for minimizing.
     * @param player The player for whom the best score is being calculated.
     * @param phase The current phase of the game.
     * @return An integer score representing the board evaluation at this depth.
     */
    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer, Player player, int phase) {
        if (depth == 0 || game.isGameOver) {
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
                            alpha = Math.max(alpha, maxEval);
                            if (beta <= alpha) {
                                break; // Beta cut-off
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
                            beta = Math.min(beta, minEval);
                            if (beta <= alpha) {
                                break; // Alpha cut-off
                            }
                        }
                    }
                }
            }
            return minEval;
        }
    }

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
            // All opponent's stones are in mills; can remove any opponent's stone
            for (Node node : board.getNodes().values()) {
                if (node.isOccupied() && node.getOccupant() == opponent) {
                    removableNodes.add(node);
                }
            }
        }
    
        if (removableNodes.isEmpty()) {
            // No opponent's stones to remove
            return null;
        }
    
        // Randomly select a node to remove
        Random random = new Random();
        int index = random.nextInt(removableNodes.size());
        return removableNodes.get(index);
    }
}
