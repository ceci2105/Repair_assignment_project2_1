package MCTS;

import java.util.*;
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
    private Node move;

    public MCTSNode(Board board, Player player, Player opponent, Node move) {
        this.board = board;
        this.player = player;
        this.opponent = opponent;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.wins = 0;
        this.move = move;
    }

    // Select child node with highest UCT value
    public MCTSNode select() {
        return children.stream()
                .max(Comparator.comparingDouble(this::uctValue))
                .orElse(this);
    }

    // Expand new moves without sequential ordering bias
    public void expand() {
        if (!children.isEmpty()) return;

        List<Node> possibleMoves = getAvailableMoves();
     //   Collections.shuffle(possibleMoves); // Prevents fixed move order bias

        for (Node move : possibleMoves) {
            Board newBoard = board.deepCopy();
            MCTSPlayer newPlayer = new MCTSPlayer(player.getName(), player.getColor());
            MCTSPlayer newOpponent = new MCTSPlayer(opponent.getName(), opponent.getColor());

            newBoard.placePiece(newPlayer, move.getId());
            newPlayer.decrementStonesToPlace();
            newPlayer.incrementStonesOnBoard();

            children.add(new MCTSNode(newBoard, newOpponent, newPlayer, move)); // Proper turn switch
        }
    }

    // Fix turn switching in simulations
    public Player simulate() {
        Board simulationBoard = board.deepCopy();
        Player currentPlayer = new MCTSPlayer(player.getName(), player.getColor()); // Use Player type
        Player otherPlayer = new MCTSPlayer(opponent.getName(), opponent.getColor());
        int turn = 0;

        while (true) {
            System.out.println("Simulating turn " + turn + " for: " + currentPlayer.getName());
            System.out.println(currentPlayer.getName() + " stones left to place: " + currentPlayer.getStonesToPlace());

            if (currentPlayer.getStonesToPlace() > 0) {
                List<Node> possibleMoves = getAvailableMoves();
                if (!possibleMoves.isEmpty()) {
                    Node randomMove = possibleMoves.get((int) (Math.random() * possibleMoves.size()));
                    simulationBoard.placePiece(currentPlayer, randomMove.getId());
                    currentPlayer.decrementStonesToPlace();
                    currentPlayer.incrementStonesOnBoard();
                    System.out.println(currentPlayer.getName() + " placed at " + randomMove.getId());
                    System.out.println(currentPlayer.getName() + " stones left to place: " + currentPlayer.getStonesToPlace());

                    if (simulationBoard.checkMill(randomMove, currentPlayer)) {
                        removeOpponentPiece(simulationBoard, otherPlayer);
                    }
                }
            }

            //Ensure turns switch correctly (something is off)
            Player temp = currentPlayer;
            currentPlayer = otherPlayer;
            otherPlayer = temp;

            // Check if the game should end
            if (!simulationBoard.hasValidMoves(currentPlayer) || currentPlayer.getStonesOnBoard() <= 2) {
                System.out.println(otherPlayer.getName() + " wins!");
                return otherPlayer;
            }
            if (!simulationBoard.hasValidMoves(otherPlayer) || otherPlayer.getStonesOnBoard() <= 2) {
                System.out.println(currentPlayer.getName() + " wins!");
                return currentPlayer;
            }

            turn++;

            if (turn > 50) {
                System.out.println("Simulation stuck! Breaking out.");
                return currentPlayer;
            }
        }
    }

    // Removes a single opponent's stone if a mill was formed
    private void removeOpponentPiece(Board board, Player opponent) {
        List<Node> opponentNodes = board.getNodesOccupiedBy(opponent);

        // Ensure there are opponent pieces to remove
        if (opponentNodes.isEmpty()) {
            System.out.println("No opponent pieces to remove.");
            return;
        }

        // Only remove a piece if a real mill was just formed
        if (!board.checkMill(opponentNodes.get(0), opponent)) {
            System.out.println("No mill formed, not removing any pieces.");
            return;
        }

        Node toRemove = opponentNodes.get((int) (Math.random() * opponentNodes.size()));
        board.removePiece(opponent, toRemove.getId());
        System.out.println("Removing " + opponent.getName() + "'s piece at " + toRemove.getId());
    }


    // Backpropagate correctly
    public void backpropagate(Player winner) {
        visits++;
        if (winner == player) wins++;
        for (MCTSNode child : children) {
            child.backpropagate(winner);
        }
    }

    // Select best move based on visit-to-win ratio
    public Node getBestMove() {
        return children.stream()
                .filter(c -> c.visits > 0)
                .max(Comparator.comparingDouble(c -> (double) c.wins / (c.visits + 1e-6)))
                .map(c -> c.move)
                .orElse(null);
    }

    private double uctValue(MCTSNode node) {
        if (node.visits == 0) return Double.MAX_VALUE;
        return (double) node.wins / node.visits + Math.sqrt(2 * Math.log(visits + 1) / (node.visits + 1));
    }

    private List<Node> getAvailableMoves() {
        List<Node> moves = new ArrayList<>(board.getNodes().values());
        moves.removeIf(Node::isOccupied);
        return moves;
    }
}
