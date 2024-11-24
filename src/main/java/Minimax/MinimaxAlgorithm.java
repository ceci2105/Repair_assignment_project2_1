package Minimax;

import game.mills.Board;
import game.mills.Game;
import game.mills.Player;
import game.mills.Node;
import lombok.extern.java.Log;

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
    private Game game;

    /**
     * Constructor to initialize MinimaxAlgorithm with a Game instance and search depth.
     * @param game The current game instance, used to access board and player information.
     * @param depth The maximum search depth for the Minimax algorithm.
     */
    public MinimaxAlgorithm(Game game, int depth) {
        this.game = game;
        this.depth = depth;
        this.evaluationFunction = new EvaluationFunction(game);
    }

    public int findBestPlacement(Board board, Player player) {
        int bestValue = Integer.MIN_VALUE;
        int bestPlacement = -1;
        Board copyBoard = board.deepCopy();
        log.log(Level.INFO, "Copy and Original are the same: {0}", copyBoard.equals(board));
        for (Node node : copyBoard.getNodes().values()) {
            if (!node.isOccupied()) {
                copyBoard.placePieceAgent(player, node.getId());
                int placementValue = evaluationFunction.evaluate(copyBoard, player, 1, node); // Evaluate
                System.out.println("Placement value " + placementValue + " NodeID: " + node.getId());
                if (placementValue > bestValue) {
                    log.log(Level.INFO, "Placement Value updated");
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
                System.out.println("From node: " + fromNode.getId());
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
        return bestMove;
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
    private int minimax(Board board, int depth, boolean isMaximizingPlayer, Player player, int phase) {
        // Base case: evaluate board if maximum depth reached or game is over
        if (depth == 0 || game.isGameOver) {
            return evaluationFunction.evaluate(board, player, phase, null);
        }

        // Maximizing player (AI) - aims to maximize score
        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE; // Initialize with minimum possible value

            // Loop over each node occupied by the maximizing player
            for (Node fromNode : board.getNodes().values()) {
                if (fromNode.getOccupant() == player) {
                    for (Node toNode : board.getNeighbours(fromNode)) {
                        if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                            // Apply the move temporarily
                            board.movePiece(player, fromNode.getId(), toNode.getId());
                            // Evaluate move with recursive call, now minimizing opponent's turn
                            int eval = minimax(board, depth - 1, false, player, phase);
                            // Undo the move
                            board.movePiece(player, toNode.getId(), fromNode.getId());
                            // Update maxEval if a higher evaluation score is found
                            maxEval = Math.max(maxEval, eval);
                        }
                    }
                }
            }
            return maxEval;

            // Minimizing player (opponent) - aims to minimize score
        } else {
            int minEval = Integer.MAX_VALUE; // Initialize with maximum possible value
            Player opponent = game.getOpponent(player); // Get the opponent player

            // Loop over each node occupied by the minimizing player
            for (Node fromNode : board.getNodes().values()) {
                if (fromNode.getOccupant() == opponent) {
                    for (Node toNode : board.getNeighbours(fromNode)) {
                        if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                            // Apply the move temporarily
                            board.movePiece(opponent, fromNode.getId(), toNode.getId());
                            // Evaluate move with recursive call, now maximizing player's turn
                            int eval = minimax(board, depth - 1, true, player, phase);
                            // Undo the move
                            board.movePiece(opponent, toNode.getId(), fromNode.getId());
                            // Update minEval if a lower evaluation score is found
                            minEval = Math.min(minEval, eval);
                        }
                    }
                }
            }
            return minEval;
        }
    }

}