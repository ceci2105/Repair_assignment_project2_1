package game.mills;

import java.util.logging.Level;
import java.util.logging.Logger;

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
    private int totalMoves;
    private int phase;
    private boolean millFormed = false;

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
        } else {
            throw new InvalidMove("Move is invalid, or flying is not allowed!");
        }
    }

    public void removeOpponentStone(int nodeID) {
        Node node = board.getNode(nodeID);
        if (node.isOccupied() && node.getOccupant() != currentPlayer) {
            Player opponent = node.getOccupant();
            node.setOccupant(null);
            opponent.decrementStonesOnBoard();
            millFormed = false; // Reset flag after removal
            logger.log(Level.ALL, "Player {0}'s stone at node {1} has been removed.", new Object[]{opponent.getName(), nodeID});
        } else {
            throw new InvalidMove("Cannot remove this stone.");
        }
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
}
