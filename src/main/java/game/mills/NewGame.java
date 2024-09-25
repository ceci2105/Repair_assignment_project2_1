package game.mills;

import java.util.logging.Level;
import java.util.logging.Logger;

import gui.MillGameUI;

/**
 * Implementation with graphing lib
 */
public class NewGame {
    private static Logger logger = Logger.getLogger(NewGame.class.getName());
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Board board;
    private MoveValidator moveValidator;
    @SuppressWarnings("unused")
    private int totalMoves;
    private int phase;
    private boolean millFormed = false;
    private MillGameUI ui;

    public NewGame(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1;
        this.board = new Board();
        this.moveValidator = new MoveValidator(board);
        this.phase = 1;
        this.totalMoves = 0;
    }

    /**
     * Switches the player when the turn changes.
     */
    public void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;

    }

    /**
     * Places a piece at the given Node ID
     * @param nodeID The node ID.
     */
    public void placePiece(int nodeID) {
        if (moveValidator.isValidPlacement(currentPlayer, nodeID)) {
            board.placePiece(currentPlayer, nodeID);
            if (board.checkMill(board.getNode(nodeID), currentPlayer)) {
                logger.log(Level.ALL, "Player {0} made a mill!", currentPlayer.getName());
                millFormed = true;
            } else {
                switchPlayer();
            }
            totalMoves++;
            checkPhase();
        } else {
            throw new InvalidMove("Placement is invalid!");
        }
    }

    /**
     * Helper function to update the phase.
     */
    public void updatePhase() {
        phase++;
    }

    /**
     * Function to make a move.
     * @param fromID Node ID from where the piece is moved.
     * @param toID Node ID to where the piece will move.
     */
    public void makeMove(int fromID, int toID) {
        if (moveValidator.isValidMove(currentPlayer, fromID, toID)) {
            board.movePiece(currentPlayer, fromID, toID);
            if (board.checkMill(board.getNode(toID), currentPlayer)) {
                logger.log(Level.ALL, "Player {0} made a mill!", currentPlayer.getName());
                millFormed = true;
            } else {
                switchPlayer();
            }
            checkGameOver();
        } else {
            throw new InvalidMove("Move is invalid, or flying is not allowed!");
        }
    }

    public void removeOpponentStone(int nodeID) {
        Node node = board.getNode(nodeID);
        Player opponent = node.getOccupant();
        
        if (node.isOccupied() && opponent != currentPlayer) {
            // Check if the stone is part of a mill
            if (board.checkMill(node, opponent)) {
                // Check if opponent has other stones that are not in mills
                boolean canRemoveMillStone = board.allOpponentStonesInMill(opponent);
                if (canRemoveMillStone) {
                    // If all stones are in mills, allow removal
                    removeStone(node, opponent);
                } else {
                    // If not all stones are in mills, disallow removal
                    throw new InvalidMove("Cannot remove a stone from a mill while other stones are available.");
                }
            } else {
                // If stone is not part of a mill, allow removal
                removeStone(node, opponent);
            }
            
            // Check phase and game over after removing a stone
            if (phase >= 2) { // Only check game over when phase 2 has started
                checkGameOver();
            }
            
            // Switch the turn after removing the stone
            switchPlayer();
            if (ui != null) {
                ui.updateGameStatus("Turn: " + currentPlayer.getName());
            }
        } else {
            throw new InvalidMove("Cannot remove this stone.");
        }
    }
    

    private void removeStone(Node node, Player opponent) {
        node.setOccupant(null);
        opponent.decrementStonesOnBoard();
        millFormed = false; // Reset flag after removal
        logger.log(Level.ALL, "Player {0}'s stone at node {1} has been removed.", new Object[]{opponent.getName(), node.getId()});
        checkGameOver();
    }
    
    public boolean isPlacingPhase() {
        return phase == 1;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Board getBoard() {
        return board;
    }

    public boolean canFly(Player currentPlayer) {
        logger.log(Level.INFO, "Player {0} has {1} stones on board.", new Object[]{currentPlayer.getName(), currentPlayer.getStonesOnBoard()});
        boolean canFly = moveValidator.canFly(currentPlayer);  // Player can fly if they have exactly 3 stones
        logger.log(Level.INFO, "Player {0} canFly: {1}", new Object[]{currentPlayer.getName(), canFly});
        return canFly;
    }

    private void checkPhase() {
        if (player1.getStonesToPlace() == 0 && player2.getStonesToPlace() == 0) {
            phase = 2;  // Transition to moving phase once all pieces are placed
        }
    }

    public int getPhase() {
        return phase;
    }

    public boolean isMillFormed() {
        return millFormed;
    }

    public void setMillFormed(boolean millFormed) {
        this.millFormed = millFormed;
    }

    private void checkGameOver() {
        // Check if we're in phase 2 (moving phase) before considering stones on board
        if (phase >= 2) {
            // Check if any player has 2 or fewer stones
            if (player1.getStonesOnBoard() <= 2) {
                gameOver(player2); // Player 2 wins if Player 1 has 2 or fewer stones
                return;
            } else if (player2.getStonesOnBoard() <= 2) {
                gameOver(player1); // Player 1 wins if Player 2 has 2 or fewer stones
                return;
            }
        }
    
        // Check if any player has no valid moves left
        if (!board.hasValidMoves(player1)) {
            gameOver(player2); // Player 2 wins if Player 1 has no valid moves
        } else if (!board.hasValidMoves(player2)) {
            gameOver(player1); // Player 1 wins if Player 2 has no valid moves
        }
    }

    private void gameOver(Player winner) {
        logger.log(Level.INFO, "Game Over! {0} wins!", new Object[]{winner.getName()});
        if (ui != null) {
            ui.displayGameOverMessage(winner);  // Display the game-over message
        }
    }

    public void setUI(MillGameUI ui) {
        this.ui = ui;
    }

}
