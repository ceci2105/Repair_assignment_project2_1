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
import java.util.logging.Level;

/**
 * The MinimaxAlgorithm class implements the Minimax algorithm to determine the best move for the AI player.
 * It evaluates potential moves up to a given depth and chooses the move with the optimal outcome for the player.
 * This class includes both the primary Minimax algorithm with recursive depth-limited searc h and evaluation function.
 */
@Log
public class MinimaxAlgorithm {
    private final int depth;
    private EvaluationFunction evaluationFunction;
    @Setter
    private Game game;

    /**
     * Constructor to initialize MinimaxAlgorithm with a Game instance and search depth.
     * @param depth The maximum search depth for the Minimax algorithm.
     */
    public MinimaxAlgorithm(int depth, EvaluationFunction evaluationFunction, Game game) {
        this.depth = depth;
        this.game = game;
        this.evaluationFunction = new EvaluationFunction(game);
    }

    public int findBestPlacement(Board board, Player player) {
        int bestValue = Integer.MIN_VALUE;
        int bestPlacement = -1;
        Board copyBoard = board.deepCopy();
        for (Node node : copyBoard.getNodes().values()) {
            if (!node.isOccupied()) {
                copyBoard.placePieceAgent(player, node.getId());
                int placementValue = evaluationFunction.evaluate(copyBoard, player, 1, node); // Evaluate
                // log.log(Level.INFO, "Val: {0}", placementValue);
                if (placementValue > bestValue) {
                    bestValue = placementValue;
                    bestPlacement = node.getId();
                }
                Node tempNode = copyBoard.getNode(node.getId());
                tempNode.setOccupant(null);
            }
        }
        log.log(Level.INFO, "Best placement {0}", bestPlacement);
        return bestPlacement;
    }

    /**
     * Finds the best move for the given player using the Minimax algorithm.
     * @param board The current game board.
     * @param player The player for whom the best move is being calculated.
     * @param phase The current phase of the game (placement, movement, or endgame).
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
                        int moveValue = minimax(board, depth - 1, false, player, phase);
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
        log.log(Level.INFO, "Best Value: {0}", bestValue);
        return bestMove;
    }


    private int minimax(Board board, int depth, boolean isMaximizingPlayer, Player player, int phase) {
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        return minimax(board, depth, isMaximizingPlayer, player, phase, alpha, beta);
    }
    /**
     * Recursive Minimax function to evaluate board states up to a certain depth.
     * Alternates between maximizing and minimizing to simulate both players' moves.
     * @param board The current game board.
     * @param depth The remaining depth for the recursive search.
     * @param isMaximizingPlayer True if the current move is for the maximizing player, false for minimizing.
     * @param player The player for whom the best score is being calculated.
     * @param phase The current phase of the game.
     * @return An integer score representing the board evaluation at this depth.
     */
    private int minimax(Board board, int depth, boolean isMaximizingPlayer, Player player, int phase, int alpha, int beta) {
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
                            int eval = minimax(board, depth - 1, false, player, phase);
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
            int minEval = Integer.MAX_VALUE;
            Player opponent = game.getOpponent(player);

            for (Node fromNode : board.getNodes().values()) {
                if (fromNode.getOccupant() == opponent) {
                    for (Node toNode : board.getNeighbours(fromNode)) {
                        if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                            board.movePiece(opponent, fromNode.getId(), toNode.getId());
                            int eval = minimax(board, depth - 1, true, player, phase);
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