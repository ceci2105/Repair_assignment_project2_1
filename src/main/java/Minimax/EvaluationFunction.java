package Minimax;

import game.mills.Board;
import game.mills.Node;
import game.mills.Player;
import game.mills.Game;

public class EvaluationFunction {

    private final Game game;

    // Constructor to initialize with Game instance
    public EvaluationFunction(Game game) {
        this.game = game;
    }

    public int evaluate(Board board, Player player, int phase) {
        switch (phase) {
            case 1:
                return evaluatePlacementPhase(board, player);
            case 2:
                return evaluateMovementPhase(board, player);
            case 3:
                return evaluateEndgamePhase(board, player);
            default:
                return 0;
        }
    }

    private int evaluatePlacementPhase(Board board, Player player) {
        int score = 0;

        for (Node node : board.getNodes().values()) {
            if (node.getOccupant() == player) {
                score += 5;

                // Reward potential mills
                if (board.checkMill(node, player)) {
                    score += 10;
                }

                // Reward flexibility in placement by favoring nodes with more neighbors
                score += board.getNeighbours(node).size();
            }
        }
        return score;
    }

    private int evaluateMovementPhase(Board board, Player player) {
        int score = 0;
        Player opponent = game.getOpponent(player);

        // Count mills and favor positions that maximize movement potential
        for (Node node : board.getNodes().values()) {
            if (node.getOccupant() == player) {
                if (board.checkMill(node, player)) {
                    score += 20;  // Reward mills
                }
                // Reward mobility based on unoccupied neighboring nodes
                for (Node neighbor : board.getNeighbours(node)) {
                    if (!neighbor.isOccupied()) {
                        score += 5;
                    }
                }
            }
        }

        // Penalize the opponent's mobility
        if (!board.hasValidMoves(opponent)) {
            score += 50; // High reward if opponent has no valid moves
        }

        return score;
    }

    private int evaluateEndgamePhase(Board board, Player player) {
        int score = 0;
        Player opponent = game.getOpponent(player);

        // Check for winning conditions (opponent has less than three pieces or no valid moves)
        if (!board.hasValidMoves(opponent) || countPieces(board, opponent) < 3) {
            return Integer.MAX_VALUE; // Winning condition
        } else if (!board.hasValidMoves(player) || countPieces(board, player) < 3) {
            return Integer.MIN_VALUE; // Losing condition
        }

        // Reward mills and remaining piece count
        for (Node node : board.getNodes().values()) {
            if (node.getOccupant() == player && board.checkMill(node, player)) {
                score += 30;
            }
        }

        score += (countPieces(board, player) - countPieces(board, opponent)) * 15;
        return score;
    }

    private int countPieces(Board board, Player player) {
        // Counts pieces by iterating over the nodes and checking occupancy
        int count = 0;
        for (Node node : board.getNodes().values()) {
            if (node.getOccupant() == player) {
                count++;
            }
        }
        return count;
    }
}
