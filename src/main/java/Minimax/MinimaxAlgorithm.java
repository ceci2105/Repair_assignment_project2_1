package Minimax;

import game.mills.Board;
import game.mills.Game;
import game.mills.Player;
import game.mills.Node;
import java.util.List;

public class MinimaxAlgorithm {
    private int depth;
    private EvaluationFunction evaluationFunction;
    private Game game;

    // Constructor that accepts a Game instance and depth
    public MinimaxAlgorithm(Game game, int depth) {
        this.depth = depth;
        this.game = game;
        this.evaluationFunction = new EvaluationFunction(game);
    }

    public Node[] findBestMove(Board board, Player player, int phase) {
        Node[] bestMove = new Node[2];  // Array to hold fromNode and toNode
        int bestValue = Integer.MIN_VALUE;

        for (Node fromNode : board.getNodes().values()) {
            if (fromNode.getOccupant() == player) {
                for (Node toNode : board.getNeighbours(fromNode)) {
                    if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                        // Apply the move
                        board.movePiece(player, fromNode.getId(), toNode.getId());
                        int moveValue = minimax(board, depth - 1, false, player, phase);
                        // Undo the move
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

    private int minimax(Board board, int depth, boolean isMaximizingPlayer, Player player, int phase) {
        if (depth == 0 || game.isGameOver) {
            return evaluationFunction.evaluate(board, player, phase);
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
                        }
                    }
                }
            }
            return minEval;
        }
    }
}
