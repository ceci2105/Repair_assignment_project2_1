package game.mills;

/**
 * Implementation with graphing lib
 */
public class NewGame {
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Board board;
    private MoveValidator moveValidator;
    private int phase;

    public NewGame(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1;
        this.board = new Board();
        this.moveValidator = new MoveValidator(board);
        this.phase = 1;
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
                // TODO: Handle mill!
            }
            switchPlayer();
            //updatePhase();
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
                // TODO: Handle mill!
            }
        }else {
            throw new InvalidMove("Move is invalid!");
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
        return moveValidator.canFly(currentPlayer);
    }
}
