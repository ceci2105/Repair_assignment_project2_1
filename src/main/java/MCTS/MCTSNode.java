package MCTS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import game.mills.Board;
import game.mills.Node;
import game.mills.Player;

class MCTSNode {
    private Board board;
    private Player player;
    private Player opponent;
    private List<MCTSNode> children;
    private int visits;
    private int wins;

    public MCTSNode(Board board, Player player, Player opponent) {
        this.board = board;
        this.player = player;
        this.opponent = opponent;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.wins = 0;
    }

    public MCTSNode select() {
        return children.stream().max(Comparator.comparingDouble(this::uctValue)).orElse(this);
    }

    public void expand() {
        List<Node> possibleMoves = getAvailableMoves();
        for (Node move : possibleMoves) {
            Board newBoard = board.deepCopy();
            newBoard.placePiece(player, move.getId());
            children.add(new MCTSNode(newBoard, player,opponent));
        }
    }

    public Player simulate() {
        Board simulationBoard = board.deepCopy();
        Player currentPlayer = player;
        while (!simulationBoard.hasValidMoves(currentPlayer)) {
            currentPlayer = (currentPlayer == player) ? opponent : player;
        }
        return currentPlayer;
    }

    public void backpropagate(Player winner) {
        visits++;
        if (winner == player) {
            wins++;
        }
        if (!children.isEmpty()) {
            children.forEach(child -> child.backpropagate(winner));
        }
    }

    public Node getBestMove() {
        return children.stream().max(Comparator.comparingInt(c -> c.wins)).map(c -> c.board.getNode(0)).orElse(null);
    }

    private double uctValue(MCTSNode node) {
        if (node.visits == 0) {
            return Double.MAX_VALUE;
        }
        return (double) node.wins / node.visits + Math.sqrt(2 * Math.log(visits) / node.visits);
    }

    private List<Node> getAvailableMoves() {
        List<Node> availableMoves = new ArrayList<>();
        for (Node node : board.getNodes().values()) {
            if (!node.isOccupied()) {
                availableMoves.add(node);
            }
        }
        return availableMoves;
    }
}
