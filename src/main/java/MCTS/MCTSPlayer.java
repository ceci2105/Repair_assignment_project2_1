package MCTS;

import game.mills.*;
import gui.MillGameUI;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import minimax.MinimaxAlgorithm;
import javafx.application.Platform;

import java.util.Collection;
import java.util.Random;
import java.util.logging.Level;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import minimax.MinimaxAlgorithm;
import java.util.List;
import java.util.ArrayList;

/**
 * The MCTSPlayer class represents an AI-controlled player that uses the Monte Carlo Tree Search (MCTS) algorithm
 * to determine the best moves in a game. The AI evaluates potential moves based on the board state, game phase, 
 * and opponent's positioning, making strategic decisions accordingly.
 */
@Slf4j
public class MCTSPlayer implements Player {
    private static final int SIMULATION_COUNT = 200; // Reduced for faster response
    
    @Getter @Setter
    private String name;
    @Getter @Setter
    private Color color;
    @Getter
    private int stonesToPlace;
    @Getter
    private int stonesOnBoard;
    @Setter
    private Game game;

    /**
     * Constructor to initialize the MCTSPlayer with a given name and color.
     *
     * @param name  The name of the AI player.
     * @param color The color representing the AI player's pieces on the board.
     * @param game  The current game instance.
     */
    public MCTSPlayer(String name, Color color, Game game) {
        this.name = name;
        this.color = color;
        this.game = game;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
    }

     /**
     * Determines the best move based on the current game phase and board state using Monte Carlo Tree Search (MCTS).
     * The method considers different phases: 
     * - Placement phase (placing stones on the board).
     * - Movement phase (moving stones between positions).
     * If no valid MCTS move is found, it falls back to a strategic or random move.
     *
     * @param board The current game board.
     * @param phase The current game phase (1: Placement, 2+: Movement).
     */

    public void makeMove(Board board, int phase) {
        try {
            // First, check if we're in the placement phase but have no stones to place
            if (stonesToPlace <= 0 && phase == 1) {
                log.warn("Player {} has no stones left to place but game is still in phase 1", name);
                // Try to move existing stones even in phase 1 if we have no stones to place
                makeStrategicMove(board);
                return;
            }
            
            if (stonesToPlace > 0 && phase == 1) {
                // Placement phase
                if (stonesToPlace == 9) {
                    // First move - try corners first
                    int[] cornerPositions = {0, 2, 6, 8, 16, 18, 22, 23};
                    Random r = new Random();
                    
                    // Try corners first
                    for (int pos : cornerPositions) {
                        if (!board.getNode(pos).isOccupied()) {
                            try {
                                game.placePiece(pos);
                                log.info("Placed first piece at position: " + pos);
                                return;
                            } catch (InvalidMove e) {
                                continue;
                            }
                        }
                    }

                    
                    // If no corner available, try any position
                    List<Integer> availablePositions = new ArrayList<>();
                    for (int i = 0; i < 24; i++) {
                        if (!board.getNode(i).isOccupied()) {
                            availablePositions.add(i);
                        }
                    }
  
                    
                    if (!availablePositions.isEmpty()) {
                        int randomIndex = r.nextInt(availablePositions.size());
                        int pos = availablePositions.get(randomIndex);
                        game.placePiece(pos);
                        log.info("Placed first piece at random position: " + pos);
                    }
                    return;
                }


                
                // Run MCTS for placement
                MCTSNode root = new MCTSNode(board, this, game, phase);
                for (int i = 0; i < SIMULATION_COUNT; i++) {
                    MCTSNode selected = root.select();
                    selected.expand();
                    double result = selected.simulate();
                    selected.backpropagate(result);
                }
                
                Node bestNode = root.findBestPlacement();
                if (bestNode != null) {
                    try {
                        game.placePiece(bestNode.getId());
                        log.info("Placed piece using MCTS at position: " + bestNode.getId());
                        if (game.isMillFormed()) {
                            handleMillFormation(board);
                        }
                        return;
                    } catch (InvalidMove e) {
                        log.error("MCTS placement failed: {}", e.getMessage());
                    }
                }

                
                // Fallback to strategic placement
                makeStrategicPlacement(board);
            } else if (phase >= 2) {
                // Movement phase
                MCTSNode root = new MCTSNode(board, this, game, phase);
                for (int i = 0; i < SIMULATION_COUNT; i++) {
                    MCTSNode selected = root.select();
                    selected.expand();
                    double result = selected.simulate();
                    selected.backpropagate(result);
                }
                
                Node[] bestMove = root.getBestMoveForMovePhase();
                if (bestMove != null && bestMove[0] != null && bestMove[1] != null) {
                    try {
                        game.makeMove(bestMove[0].getId(), bestMove[1].getId());
                        if (game.isMillFormed()) {
                            handleMillFormation(board);
                        }
                        return;
                    } catch (InvalidMove e) {
                        log.error("MCTS move failed: {}", e.getMessage());
                    }
                }
                
                // Fallback to strategic movement
                makeStrategicMove(board);
            } else {
                // This is a fallback for when stonesToPlace <= 0 but we're still in phase 1
                // This should not happen in normal gameplay, but provides a safety net
                log.warn("Unusual game state: No stones to place but game still in phase 1");
                // Try to move existing stones even in phase 1 if we have no stones to place
                makeStrategicMove(board);
            }
        } catch (Exception e) {
            log.error("Error in MCTS move: {}", e.getMessage());
            // Emergency fallback
            if (stonesToPlace > 0) {
                makeRandomPlacement(board);
            } else {
                makeRandomMove(board);
            }
        }
    }

     /**
     * Attempts to place a piece strategically to form a mill or gain an advantage.
     * If no stones are left to place, it logs an error and falls back to making a strategic move instead.
     * 
     * The method first checks if a mill can be completed by looking for a position where 
     * the AI already has two pieces in a row and only one empty spot remains.
     * If such a spot is found, the AI places a piece there.
     * If no mill can be completed, it falls back to a random placement.
     *
     * @param board The current game board.
     */

    private void makeStrategicPlacement(Board board) {
        if (stonesToPlace <= 0) {
            log.error("No stones left to place in strategic placement");
            // Try to move existing stones instead
            makeStrategicMove(board);
            return;
        }

        // Try to complete a mill first
        for (int[] mill : board.getMills()) {
            int emptyCount = 0;
            int playerCount = 0;
            int emptyPos = -1;
            
            for (int pos : mill) {
                Node node = board.getNode(pos);
                if (!node.isOccupied()) {
                    emptyCount++;
                    emptyPos = pos;
                } else if (node.getOccupant() == this) {
                    playerCount++;
                }
            }
            
            if (emptyCount == 1 && playerCount == 2) {
                try {
                    game.placePiece(emptyPos);
                    log.info("Placed piece strategically at position: " + emptyPos);
                    return;
                } catch (InvalidMove e) {
                    continue;
                }
            }
        }
        
        // If can't complete mill, try random placement
        makeRandomPlacement(board);
    }

    /**
     * Attempts to move a piece strategically to form a mill or gain an advantage.
     * The method first checks if moving a piece can complete a mill by looking for a valid move
     * that places an AI-controlled piece into a mill formation.
     * If such a move is found, it is executed.
     * If no strategic move is found, it falls back to a random move.
     *
     * @param board The current game board.
     */

    private void makeStrategicMove(Board board) {
        // Try to complete a mill first
        for (Node fromNode : board.getNodes().values()) {
            if (fromNode.getOccupant() == this) {
                Collection<Node> destinations = stonesOnBoard <= 3 ? 
                    board.getNodes().values() : board.getNeighbours(fromNode);
                    
                for (Node toNode : destinations) {
                    if (!toNode.isOccupied()) {
                        Board tempBoard = board.deepCopy();
                        tempBoard.movePiece(this, fromNode.getId(), toNode.getId());
                        if (tempBoard.checkMill(toNode, this)) {
                            try {
                                game.makeMove(fromNode.getId(), toNode.getId());
                                return;
                            } catch (InvalidMove e) {
                                continue;
                            }
                        }
                    }
                }
            }
        }
        
        // If can't complete mill, try random move
        makeRandomMove(board);
    }

     /**
     * Attempts to place a piece at a random available position on the board.
     * If no stones are left to place, it logs an error and exits.
     * The method iterates through all board positions to find available ones.
     * It then randomly selects an available position and attempts to place a piece.
     * If an invalid move is encountered, the position is removed from consideration,
     * and another random position is tried until a valid move is found or no positions remain.
     *
     * @param board The current game board.
     */

    private void makeRandomPlacement(Board board) {
        if (stonesToPlace <= 0) {
            log.error("No stones left to place in random placement");
            return;
        }

        List<Integer> availablePositions = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (!board.getNode(i).isOccupied()) {
                availablePositions.add(i);
            }
        }
        
        while (!availablePositions.isEmpty()) {
            int randomIndex = new Random().nextInt(availablePositions.size());
            int position = availablePositions.get(randomIndex);
            try {
                game.placePiece(position);
                log.info("Placed piece randomly at position: " + position);
                return;
            } catch (InvalidMove e) {
                availablePositions.remove(randomIndex);
            }
        }
    }

    /**
     * Attempts to move a piece from one position to another randomly chosen valid position.
     * If there are no stones on the board, it logs an error and exits.
     * The method identifies all possible moves by iterating over occupied nodes.
     * If the player has three stones left, all nodes are considered as destinations.
     * Otherwise, only adjacent nodes are considered.
     * A random move is attempted from the list of possible moves.
     * If an invalid move occurs, the move is removed from the list, and another is tried.
     *
     * @param board The current game board.
     */

    private void makeRandomMove(Board board) {
        if (stonesOnBoard == 0) {
            log.error("Cannot make a move with no stones on the board");
            return;
        }
        
        List<Node[]> possibleMoves = new ArrayList<>();
        for (Node fromNode : board.getNodes().values()) {
            if (fromNode.getOccupant() == this) {
                Collection<Node> destinations = stonesOnBoard <= 3 ? 
                    board.getNodes().values() : board.getNeighbours(fromNode);
                    
                for (Node toNode : destinations) {
                    if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                        possibleMoves.add(new Node[]{fromNode, toNode});
                    }
                }
            }
        }
        
        if (possibleMoves.isEmpty()) {
            log.error("No valid moves available for {}", name);
            return;
        }
        
        while (!possibleMoves.isEmpty()) {
            int randomIndex = new Random().nextInt(possibleMoves.size());
            Node[] move = possibleMoves.get(randomIndex);
            try {
                game.makeMove(move[0].getId(), move[1].getId());
                log.info("Made random move from {} to {}", move[0].getId(), move[1].getId());
                return;
            } catch (InvalidMove e) {
                log.warn("Invalid random move attempt: {}", e.getMessage());
                possibleMoves.remove(randomIndex);
            }
        }
    }

    /**
     * Handles the formation of a mill by removing an opponent's piece.
     * Prioritizes removing a piece that is not part of a mill, if possible.
     *
     * @param board The current game board.
     */

    private void handleMillFormation(Board board) {
        Player opponent = game.getOpponent(this);
        boolean removedPiece = false;

        // First try to remove pieces that are not part of a mill
        for (Node node : board.getNodes().values()) {
            if (node.isOccupied() && node.getOccupant() == opponent && !board.isPartOfMill(node)) {
                try {
                    game.removePiece(node.getId());
                    removedPiece = true;
                    break;
                } catch (InvalidMove e) {
                    // Try next piece
                }
            }
        }

        // If all pieces are in mills, remove any opponent piece
        if (!removedPiece) {
            for (Node node : board.getNodes().values()) {
                if (node.isOccupied() && node.getOccupant() == opponent) {
                    try {
                        game.removePiece(node.getId());
                        break;
                    } catch (InvalidMove e) {
                        // Try next piece
                    }
                }
            }
        }
    }


    /**
     * Decrements the number of stones left to place and increments the number of stones on the board.
     */

    @Override
    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

     /**
     * Increments the number of stones on the board.
     */

    @Override
    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    /**
     * Decrements the number of stones on the board.
     */

    @Override
    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }
}

