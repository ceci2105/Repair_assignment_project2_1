package Minimax;

import game.mills.Board;
import game.mills.Game;
import game.mills.Player;
import game.mills.Node;

/**
 * The MinimaxAlgorithm class implements the Minimax algorithm to determine the best move for the AI player.
 * It evaluates potential moves up to a given depth and chooses the move with the optimal outcome for the player.
 * This class includes both the primary Minimax algorithm with recursive depth-limited search and evaluation function.
 */
public class MinimaxAlgorithm {
    private final int depth;                       // The maximum search depth for the Minimax algorithm
    private EvaluationFunction evaluationFunction; // Instance of EvaluationFunction to score board states
    private final Game game;                       // The current game instance

    /**
     * Constructor to initialize MinimaxAlgorithm with a Game instance and search depth.
     * @param game The current game instance, used to access board and player information.
     * @param depth The maximum search depth for the Minimax algorithm.
     */
    public MinimaxAlgorithm(Game game, int depth) {
        this.game = game;
        this.depth = depth;
        this.evaluationFunction = new EvaluationFunction(game); // Initialize evaluation function for board scoring
    }

    public int findBestPlacement(Board board, Player player) {
        int bestValue = Integer.MIN_VALUE;
        int bestPlacement = -1;

        // Evaluate each empty node for potential placement
        for (Node node : board.getNodes().values()) {
            if (!node.isOccupied()) {  // Only consider empty nodes
                board.placePiece(player, node.getId());  // Temporarily place a piece
                int placementValue = evaluationFunction.evaluate(board, player, 1); // Evaluate
                game.removeStone(board.getNode(node.getId()), player);  // Remove the piece after evaluation

                if (placementValue > bestValue) {  // Update if this placement is better
                    bestValue = placementValue;
                    bestPlacement = node.getId();
                }
            }
        }
        return bestPlacement;  // Return the best placement node ID
    }

    /**
     * Finds the best move for the given player using the Minimax algorithm.
     * @param board The current game board.
     * @param player The player for whom the best move is being calculated.
     * @param phase The current phase of the game (placement, movement, or endgame).
     * @return An array containing the best fromNode and toNode representing the move.
     */
    public Node[] findBestMove(Board board, Player player, int phase) {
        Node[] bestMove = new Node[2];  // Array to store the best move's start and end nodes
        int bestValue = Integer.MIN_VALUE;  // Initialize best value for maximizing player

        // Iterate over all nodes occupied by the player
        for (Node fromNode : board.getNodes().values()) {
            
            if (fromNode.getOccupant() == player) {
                System.out.println("From node: " + fromNode.getId());
                // Evaluate each possible move from the current node to neighboring nodes
                for (Node toNode : board.getNeighbours(fromNode)) {
                    if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                        // Apply the move temporarily
                        board.movePiece(player, fromNode.getId(), toNode.getId());
                        // Evaluate the move using Minimax
                        int moveValue = minimax(board, depth - 1, false, player, phase);
                        // Undo the move
                        board.movePiece(player, toNode.getId(), fromNode.getId());

                        // If the move value is better than the best found, update bestMove
                        if (moveValue > bestValue) {
                            bestValue = moveValue;
                            bestMove[0] = fromNode;
                            bestMove[1] = toNode;
                        }
                    }
                }
            }
        }
        return bestMove; // Return the best move found
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
            return evaluationFunction.evaluate(board, player, phase);
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