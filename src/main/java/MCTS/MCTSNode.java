package MCTS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import game.mills.Board;
import game.mills.InvalidMove;
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
        if (player.getStonesToPlace() > 0) {
            System.out.println("Expanding with stone placement...");
            // Expand with placing stones
            List<Node> possibleMoves = getAvailableMoves();
            for (Node move : possibleMoves) {
                Board newBoard = board.deepCopy();
                newBoard.placePiece(player, move.getId());
                player.decrementStonesToPlace();
                player.incrementStonesOnBoard();
                children.add(new MCTSNode(newBoard, player, opponent));
                System.out.println("Placing at node: " + move.getId());
            }
        } else {
            System.out.println("Expanding with stone movement...");
            // Handle movement
            List<Node> possibleMoves = getAvailableMoves();
            for (Node move : possibleMoves) {
                Board newBoard = board.deepCopy();
                newBoard.movePiece(player, move.getId(), move.getId());
                children.add(new MCTSNode(newBoard, player, opponent));
            }
        }
    }
    


    public Player simulate() {
        Board simulationBoard = board.deepCopy();
        Player currentPlayer = player;
        Player otherPlayer = opponent;
    
        while (true) {
            // Debugging output: show the player's turn and available stones to place
            System.out.println("Player's turn: " + currentPlayer.getName());
            System.out.println("Stones to place: " + currentPlayer.getStonesToPlace());
    
            if (currentPlayer.getStonesToPlace() > 0) {
                // Debugging: Show that the player is placing a stone
                System.out.println("Placing stone...");
    
                // Collect all available moves (empty nodes) for placing stones
                List<Node> possibleMoves = new ArrayList<>();
                for (Node node : simulationBoard.getNodes().values()) {
                    if (!node.isOccupied()) {
                        possibleMoves.add(node);
                    }
                }
    
                // If there are available moves (empty nodes to place the stone)
                if (!possibleMoves.isEmpty()) {
                    // Choose a random move
                    Node randomMove = possibleMoves.get((int) (Math.random() * possibleMoves.size()));
                    System.out.println("Placing at node: " + randomMove.getId());
    
                    // Place the stone on the selected node
                    simulationBoard.placePiece(currentPlayer, randomMove.getId());
    
                    // Update the current player's stones
                    currentPlayer.decrementStonesToPlace();
                    currentPlayer.incrementStonesOnBoard();
                }
            } else {
                // It's the movement phase
                System.out.println("It's the movement phase.");
    
                // Get all available moves (empty nodes or valid movement positions)
                List<Node> possibleMoves = new ArrayList<>();
                for (Node node : simulationBoard.getNodes().values()) {
                    if (!node.isOccupied()) {
                        possibleMoves.add(node);
                    }
                }
    
                // If there are valid available moves, proceed
                if (!possibleMoves.isEmpty()) {
                    Node randomMove = possibleMoves.get((int) (Math.random() * possibleMoves.size()));
    
                    // If stones have already been placed, we perform a move
                    System.out.println("Moving stone to node: " + randomMove.getId());
                    simulationBoard.movePiece(currentPlayer, randomMove.getId(), randomMove.getId());
    
                    // Update the stones
                    currentPlayer.decrementStonesOnBoard();
                    currentPlayer.incrementStonesOnBoard();
                }
            }
    
            // Check if the game has ended (either by no valid moves or a player running out of stones)
            if (!simulationBoard.hasValidMoves(currentPlayer) || currentPlayer.getStonesOnBoard() <= 2) {
                // Other player wins
                System.out.println("Game over: " + otherPlayer.getName() + " wins!");
                return otherPlayer;
            }
            if (!simulationBoard.hasValidMoves(otherPlayer) || otherPlayer.getStonesOnBoard() <= 2) {
                // Current player wins
                System.out.println("Game over: " + currentPlayer.getName() + " wins!");
                return currentPlayer;
            }
    
            // Switch players for the next turn
            Player temp = currentPlayer;
            currentPlayer = otherPlayer;
            otherPlayer = temp;
        }
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