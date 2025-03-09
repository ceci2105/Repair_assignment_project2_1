package MCTS;

import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * The MCTSNode class implements the MCTS algorithm to determine the best move for the AI player.
 * It evaluates potential moves using the Monte Carlo Tree Search (MCTS) algorithm and 
 * interacts with the game through the board and player states.
 */
@Slf4j
public class MCTSNode {
    private static final double EXPLORATION_CONSTANT = 1.414; // Standard UCT constant
    private static final int SIMULATION_DEPTH = 20; // Reduced from 30 for better performance

    @Setter
    private Game game;

    private Board board;
    private Player currentPlayer;
    private List<MCTSNode> children;
    private int visits;
    private double value; // The total score accumulated from simulations
    private MCTSNode parent;
    private Node moveFrom;  // For movement phase
    private Node moveTo;    // For movement phase
    private Node placement; // For placement phase
    private int gamePhase;
    

     /**
     * Constructor for initializing an MCTS node with the current board and player.
     *
     * @param board The current board state.
     * @param currentPlayer The player whose turn it is.
     * @param game The game instance.
     * @param gamePhase The current phase of the game.
     */
    public MCTSNode(Board board, Player currentPlayer, Game game, int gamePhase) {
        this.board = board.deepCopy();
        this.currentPlayer = currentPlayer;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.value = 0.0;

        this.game = game;
        this.gamePhase = gamePhase;
    }

    /**
     * Phase 1: Selection - Select a node to expand based on Upper Confidence Bound for Trees (UCT).
     * The node with the highest UCT value is chosen.
     *
     * @return The selected node for expansion.
     */
    public MCTSNode select() {
        MCTSNode current = this;
        while (!current.children.isEmpty()) {
            current = current.selectBestChild();
            if (current.visits == 0) {
                return current;
            }
        }
        return current;
    }

    /**
     * Selects the best child node based on the UCT formula.
     *
     * @return The best child node.
     */
    private MCTSNode selectBestChild() {
        double bestUCT = Double.NEGATIVE_INFINITY;
        MCTSNode bestChild = null;

        for (MCTSNode child : children) {
            double uct;
            if (child.visits == 0) {
                uct = Double.POSITIVE_INFINITY;
            } else {
                uct = (child.value / child.visits) + 
                      EXPLORATION_CONSTANT * Math.sqrt(Math.log(this.visits) / child.visits);
            }

            

            if (uct > bestUCT) {
                bestUCT = uct;
                bestChild = child;
            }
        }

        return bestChild;
    }

    /**
     * Phase 2: Expansion - If the node is not fully expanded, expand by adding a child node.
     * This function adds a new child node by making a possible move on the board.
     */
    public void expand() {
        if (currentPlayer.getStonesToPlace() > 0) {
            expandPlacementPhase();
        } else {
            expandMovementPhase();
        }
    }

     /**
     * Expands the placement phase by generating child nodes for all available positions.
     */

    private void expandPlacementPhase() {
        // Only expand if the player has stones to place
        if (currentPlayer.getStonesToPlace() <= 0) {
            return;
        }

        System.out.println("expandPlacementPhase 1 "+ currentPlayer.getStonesToPlace());

        List<Integer> availablePositions = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (!board.getNode(i).isOccupied()) {
                availablePositions.add(i);
            }
        }
        
        if (!availablePositions.isEmpty()) {
            // Create child nodes for each available position
            for (int pos : availablePositions) {
                Board newBoard = board.deepCopy();
                // Create a new MCTSPlayer instance to avoid modifying the original player's state
                MCTSPlayer newPlayer = new MCTSPlayer(currentPlayer.getName(), currentPlayer.getColor(), game);
                // Set the correct number of stones
                while (newPlayer.getStonesToPlace() > currentPlayer.getStonesToPlace()) {
                    newPlayer.decrementStonesToPlace();
                    System.out.println("expandPlacementPhase nel while "+ currentPlayer.getStonesToPlace());
                }
                
                // Place the piece and update stone count
                newBoard.placePiece(newPlayer, pos);
                newPlayer.decrementStonesToPlace();
                newPlayer.incrementStonesOnBoard();

                System.out.println("expandPlacementPhase 2 "+ currentPlayer.getStonesToPlace());
                
                MCTSNode child = new MCTSNode(newBoard, game.getOpponent(currentPlayer), game, gamePhase);
                child.parent = this;
                child.placement = board.getNode(pos);
                children.add(child);
            }
        }
    }

    /**
     * Expands the movement phase by generating child nodes for all possible moves.
     */

    private void expandMovementPhase() {
        for (Node fromNode : board.getNodes().values()) {
            if (fromNode.getOccupant() == currentPlayer) {              

                Collection<Node> possibleMoves = currentPlayer.getStonesOnBoard() <= 3 ? 
                    board.getNodes().values() : board.getNeighbours(fromNode);
                    
                for (Node toNode : possibleMoves) {
                    if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                        Board newBoard = board.deepCopy();
                        // Create a new MCTSPlayer instance to avoid modifying the original player's state
                        MCTSPlayer newPlayer = new MCTSPlayer(currentPlayer.getName(), currentPlayer.getColor(), game);
                        // Set the correct number of stones
                         while (newPlayer.getStonesToPlace() > currentPlayer.getStonesToPlace()) {
                            
                            newPlayer.decrementStonesToPlace();
                        }
                        newPlayer.incrementStonesOnBoard(); // Set initial stones on board
                        
                        newBoard.movePiece(newPlayer, fromNode.getId(), toNode.getId());
                        
                        MCTSNode child = new MCTSNode(newBoard, game.getOpponent(currentPlayer), game, gamePhase);
                        child.parent = this;
                        child.moveFrom = fromNode;
                        child.moveTo = toNode;
                        children.add(child);
                    }
                }
            }
        }
    }

    /**
     * Phase 3: Simulation - Simulate a random game from the current node to a terminal state.
     * 
     * @return The result of the simulation. 1 for win, -1 for loss, 0 for draw.
     */
    public double simulate() {
        Board simBoard = board.deepCopy();
        Player simPlayer = currentPlayer;
        int moveCount = 0;
        boolean gameOver = false;

        System.out.println("SIMULATE 1 "+ currentPlayer.getStonesToPlace());
        
        // Create copies of players to track stone counts during simulation
        MCTSPlayer simCurrentPlayer = new MCTSPlayer(currentPlayer.getName(), currentPlayer.getColor(), game);
        MCTSPlayer simOpponent = new MCTSPlayer(game.getOpponent(currentPlayer).getName(), 
                                          game.getOpponent(currentPlayer).getColor(), game);
        
        // Initialize stone counts by decrementing from 9 to match current state
        while (simCurrentPlayer.getStonesToPlace() > currentPlayer.getStonesToPlace()) {
            simCurrentPlayer.decrementStonesToPlace();
        }
        while (simOpponent.getStonesToPlace() > game.getOpponent(currentPlayer).getStonesToPlace()) {
            simOpponent.decrementStonesToPlace();
        }

        System.out.println("SIMULATE 2 "+ currentPlayer.getStonesToPlace());

        // Keep track of mills formed during simulation
        int playerMillsFormed = 0;
        int opponentMillsFormed = 0;

        while (!gameOver && moveCount < SIMULATION_DEPTH) {
            Player activePlayer = (simPlayer == currentPlayer) ? simCurrentPlayer : simOpponent;
            
            if (activePlayer.getStonesToPlace() > 0) {
                // Placement phase - prioritize mill-forming moves
                List<Integer> availablePositions = new ArrayList<>();
                for (int i = 0; i < 24; i++) {
                    if (!simBoard.getNode(i).isOccupied()) {
                        availablePositions.add(i);
                    }
                }
                System.out.println("SIMULATE 3 "+ currentPlayer.getStonesToPlace());
                
                if (availablePositions.isEmpty()) break;
                
                // Try to find a position that forms a mill
                int selectedPos = -1;
                for (int pos : availablePositions) {
                    Board tempBoard = simBoard.deepCopy();
                     
                    
                   
                    if (tempBoard.checkMill(tempBoard.getNode(pos), simPlayer)) {
                        selectedPos = pos;
                        if (simPlayer == currentPlayer) playerMillsFormed++;
                        else opponentMillsFormed++;
                        break;
                    }
                }
                
                System.out.println("SIMULATE 4 "+ currentPlayer.getStonesToPlace());
                // If no mill-forming move found, choose random
                if (selectedPos == -1) {
                    selectedPos = availablePositions.get(new Random().nextInt(availablePositions.size()));
                }
                
               // simBoard.placePiece(simPlayer, selectedPos);
                if (activePlayer instanceof MCTSPlayer) {
                    ((MCTSPlayer)activePlayer).decrementStonesToPlace();
                    ((MCTSPlayer)activePlayer).incrementStonesOnBoard();
                }
               
            } else {
                
                // Movement phase - prioritize mill-forming moves and blocking opponent mills
                List<Node[]> possibleMoves = new ArrayList<>();
                for (Node fromNode : simBoard.getNodes().values()) {
                    if (fromNode.getOccupant() == simPlayer) {
                        Collection<Node> destinations = activePlayer.getStonesOnBoard() <= 3 ? 
                            simBoard.getNodes().values() : simBoard.getNeighbours(fromNode);
                            
                        for (Node toNode : destinations) {
                            if (!toNode.isOccupied() && simBoard.isValidMove(fromNode, toNode)) {
                                possibleMoves.add(new Node[]{fromNode, toNode});
                            }
                        }
                    }
                }
                
                if (possibleMoves.isEmpty()) {
                    gameOver = true;
                    break;
                }
                                
                // Try to find a move that forms a mill
                Node[] selectedMove = null;
                for (Node[] move : possibleMoves) {
                    Board tempBoard = simBoard.deepCopy();
                    tempBoard.movePiece(simPlayer, move[0].getId(), move[1].getId());
                    if (tempBoard.checkMill(move[1], simPlayer)) {
                        selectedMove = move;
                        if (simPlayer == currentPlayer) playerMillsFormed++;
                        else opponentMillsFormed++;
                        break;
                    }
                }
                
                // If no mill-forming move found, choose random
                if (selectedMove == null) {
                    selectedMove = possibleMoves.get(new Random().nextInt(possibleMoves.size()));
                }
                
                simBoard.movePiece(simPlayer, selectedMove[0].getId(), selectedMove[1].getId());
            }

            // Check win conditions
            if (activePlayer.getStonesOnBoard() <= 2 && activePlayer.getStonesToPlace() == 0) {
                gameOver = true;
                return simPlayer == currentPlayer ? -1000 : 1000;
            }

            simPlayer = (simPlayer == currentPlayer) ? game.getOpponent(currentPlayer) : currentPlayer;
            moveCount++;
        }

        return evaluatePosition(simBoard) + (playerMillsFormed - opponentMillsFormed) * 50;
    }

    /**
     * Determines whether the given player has any valid moves available.
     * A player with 3 or fewer stones can "fly" to any empty position.
     * Otherwise, the method checks if the player can move to a valid adjacent node.
     *
     * @param board  The game board containing the nodes and connections.
     * @param player The player whose possible moves are being checked.
     * @return {@code true} if the player has at least one valid move, {@code false} otherwise.
     */

    private boolean hasValidMoves(Board board, Player player) {
        if (player.getStonesOnBoard() <= 3) return true; // Can fly
        
        for (Node fromNode : board.getNodes().values()) {
            if (fromNode.getOccupant() == player) {
                for (Node toNode : board.getNeighbours(fromNode)) {
                    if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Evaluates the current board position by assigning a score based on various game factors.
     * The evaluation considers:
     * - Piece count difference (each piece difference is worth 10 points).
     * - Mill formations (each mill is worth 30 points).
     * - Winning or losing conditions (heavy penalties for having 2 or fewer pieces left).
     * - Mobility (penalties for lack of valid moves).
     *
     * @param board The game board to evaluate.
     * @return A numerical score representing the favorability of the board position
     *         for the current player. Higher scores indicate a better position.
     */

    private double evaluatePosition(Board board) {
        double score = 0.0;
        
        // Count pieces
        int playerPieces = countPieces(board, currentPlayer);
        int opponentPieces = countPieces(board, game.getOpponent(currentPlayer));
        score += (playerPieces - opponentPieces) * 10;
        
        // Count mills
        int playerMills = countMills(board, currentPlayer);
        int opponentMills = countMills(board, game.getOpponent(currentPlayer));
        score += (playerMills - opponentMills) * 30;
        
        // Check for winning position
        if (playerPieces <= 2 && currentPlayer.getStonesToPlace() == 0) {
            score -= 1000; // Heavy penalty for losing
        }
        if (opponentPieces <= 2 && game.getOpponent(currentPlayer).getStonesToPlace() == 0) {
            score += 1000; // High reward for winning
        }
        
        // Consider mobility
        if (!hasValidMoves(board, currentPlayer)) {
            score -= 500;
        }
        if (!hasValidMoves(board, game.getOpponent(currentPlayer))) {
            score += 500;
        }
        
        return score;
    }

    /**
     * Counts the number of pieces a given player has on the board.
     *
     * @param board  The game board containing the nodes.
     * @param player The player whose pieces are to be counted.
     * @return The total number of pieces the player has on the board.
     */

    private int countPieces(Board board, Player player) {
        return (int) board.getNodes().values().stream()
                .filter(node -> node.getOccupant() == player)
                .count();
    }

    /**
     * Counts the number of mills a given player has formed on the board.
     * A mill is a set of three aligned pieces belonging to the same player.
    *
    * @param board  The game board containing the nodes and mill patterns.
    * @param player The player whose mills are to be counted.
    * @return The total number of mills the player has formed.
    */

    private int countMills(Board board, Player player) {
        int mills = 0;
        for (int[] millPattern : board.getMills()) {
            boolean isMill = true;
            for (int nodeId : millPattern) {
                Node node = board.getNode(nodeId);
                if (node.getOccupant() != player) {
                    isMill = false;
                    break;
                }
            }
            if (isMill) mills++;
        }
        return mills;
    }

    /**
     * Phase 4: Backpropagation - Backpropagate the result of the simulation to update the node statistics.
     * 
     * @param result The result of the simulation (1 for win, -1 for loss, 0 for draw).
     */
    public void backpropagate(double result) {
        MCTSNode current = this;
        while (current != null) {
            current.visits++;
            current.value += result;
            current = current.parent;
            result = -result; // Negate the result for the opponent
        }
    }

    // Getters for the value and visit count (useful for the root MCTS node)
    public double getValue() {
        return visits == 0 ? 0 : value / visits; // Average value from simulations
    }

    public int getVisits() {
        return visits;
    }

    /**
     * Retrieves the best child node based on the highest visit count.
     *
     * @return The best child node.
     */
    public MCTSNode getBestChild() {
        
        if (children.isEmpty()) {
            return null;
        }
    
        MCTSNode bestChild = children.stream()
                .max((node1, node2) -> Double.compare(node1.getValue(), node2.getValue()))
                .orElse(null);
    
        if (bestChild == null) {
            System.out.println("Error: No child found!");
        } else {
            System.out.println("Best child found with value: " + bestChild.getValue());
        }
    
        return bestChild;
                
    }

    /**
     * Checks if the given player has won the game.
     *
     * @param player The player to check.
     * @return True if the player has won, false otherwise.
     */

    public boolean hasWon(Player player) {
        if (getPlayerPieceCount(game.getOpponent(player)) <= 2) {
            return true; 
        }

       
        if (!hasValidMoves(game.getOpponent(player))) {
            return true;
        }

        return false; 
    }

    /**
     * Counts the number of pieces a given player has on the board.
     *
     * @param player The player whose pieces are to be counted.
     * @return The total number of pieces the player has on the board.
     */
    private int getPlayerPieceCount(Player player) {
        int count = 0;
        for (Node node : game.getBoard().getNodes().values()) {
            if (node.getOccupant() == player) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if the given player has any valid moves left.
     *
     * @param player The player to check for available moves.
     * @return true if the player has at least one valid move, false otherwise.
     */

    private boolean hasValidMoves(Player player) {
        for (Node node : game.getBoard().getNodes().values()) {
            if (node.getOccupant() == player) {
                for (Node neighbor : game.getBoard().getNeighbours(node)) {
                    if (!neighbor.isOccupied()) {
                        return true; 
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds the best placement move based on the child nodes.
     * This method selects the child node with the highest average value.
     *
     * @return The best node for placement, or null if no children exist.
     */

    public Node findBestPlacement() {
        if (children.isEmpty()) {
            return null;
        }

        MCTSNode bestChild = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (MCTSNode child : children) {
            // Use visits and value to determine best move
            if (child.visits > 0) {
                double nodeValue = child.value / child.visits;
                if (nodeValue > bestValue) {
                    bestValue = nodeValue;
                    bestChild = child;
                }
            }
        }

        return bestChild != null ? bestChild.placement : null;
    }

    /**
     * Determines the best move in the move phase using UCT (Upper Confidence Bound 1).
     * This considers both exploitation (average value) and exploration (visit count).
     *
     * @return An array containing the move's starting node and destination node,
     *         or null if no valid moves are found.
     */
   
    public Node[] getBestMoveForMovePhase() {
        if (children.isEmpty()) return null;
        
        MCTSNode bestChild = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (MCTSNode child : children) {
            if (child.visits > 0) {
                // Use UCB1 formula for final move selection
                double exploitation = child.value / child.visits;
                double exploration = Math.sqrt(Math.log(this.visits) / child.visits);
                double score = exploitation + EXPLORATION_CONSTANT * exploration;
                
                if (score > bestScore) {
                    bestScore = score;
                    bestChild = child;
                }
            }
        }
        
        return bestChild != null ? new Node[]{bestChild.moveFrom, bestChild.moveTo} : null;
    }

    

     /**
      * Finds the best move based on the number of visits to child nodes.
      * This method selects the most visited child node, assuming it is the most promising.
      *
      * @return An array containing the best move's starting node and destination node,
      *         or null if no valid moves exist.
      */
    public Node[] findBestMove() {
        if (children.isEmpty()) return null;
        
        MCTSNode bestChild = children.stream()
                .max((a, b) -> Double.compare(a.visits, b.visits))
                .orElse(null);
                
        return bestChild != null ? new Node[]{bestChild.moveFrom, bestChild.moveTo} : null;
    }

}