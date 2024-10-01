package game.mills;

import java.util.logging.Level;
import java.util.logging.Logger;
import gui.MillGameUI;

/**
 * The NewGame class manages the game logic for Mills.
 * It controls the game phases, player turns, moves, and interactions with the game board.
 */
public class NewGame {
    private static Logger logger = Logger.getLogger(NewGame.class.getName());
    private Player humanPlayer1;
    private Player humanPlayer2;
    private Player currentPlayer;
    private Board board;
    private MoveValidator moveValidator;
    @SuppressWarnings("unused")
    private int totalMoves;
    private int phase;
    private boolean millFormed = false;
    private MillGameUI ui;

    /**
     * Constructs a new game instance with two players.
     * Initializes the board and sets up the game for the placing phase.
     *
     * @param p1 the first player.
     * @param p2 the second player.
     */
    public NewGame(Player p1, Player p2) {
        this.humanPlayer1 = p1;
        this.humanPlayer2 = p2;
        this.currentPlayer = p1;
        this.board = new Board();
        this.moveValidator = new MoveValidator(board);
        this.phase = 1; //Start the game in the placing phase
        this.totalMoves = 0;
    }

    /**
     * Switches the player when the turn changes.
     */
    public void switchPlayer() {
        currentPlayer = (currentPlayer == humanPlayer1) ? humanPlayer2 : humanPlayer1;

    }

    /**
     * Places a piece on the board at the specified node ID.
     * Validates the move and checks if a mill has been formed.
     *
     * @param nodeID the node ID where the piece is to be placed.
     * @throws InvalidMove if the move is not valid.
     */
    public void placePiece(int nodeID) {
        if (moveValidator.isValidPlacement(currentPlayer, nodeID)) {
            board.placePiece(currentPlayer, nodeID);
            if (board.checkMill(board.getNode(nodeID), currentPlayer)) {
                logger.log(Level.ALL, "HumanPlayer {0} made a mill!", currentPlayer.getName());
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
     * Updates the current game phase
     */
    public void updatePhase() {
        phase++;
    }

    /**
     * Executes a move from one node to another.
     * Validates the move and checks if a mill is formed after the move.
     *
     * @param fromID the node ID where the piece is moved from.
     * @param toID   the node ID where the piece is moved to.
     * @throws InvalidMove if the move is not valid or flying is not allowed.
     */
    public void makeMove(int fromID, int toID) {
        if (moveValidator.isValidMove(currentPlayer, fromID, toID)) {
            board.movePiece(currentPlayer, fromID, toID);
            if (board.checkMill(board.getNode(toID), currentPlayer)) {
                logger.log(Level.ALL, "HumanPlayer {0} made a mill!", currentPlayer.getName());
                millFormed = true;
            } else {
                switchPlayer();
            }
            checkGameOver();
        } else {
            throw new InvalidMove("Move is invalid, or flying is not allowed!");
        }
    }

    /**
     * Removes an opponent's stone from the board, if permitted.
     *
     * @param nodeID the node ID where the opponent's stone is located.
     * @throws InvalidMove if the removal is not allowed.
     */
    public void removeOpponentStone(int nodeID) {
        Node node = board.getNode(nodeID);
        Player opponent = node.getOccupant();
        
        if (node.isOccupied() && opponent != currentPlayer) {
            if (board.checkMill(node, opponent)) {
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
            
            // Checking phase and game over after removing a stone
            if (phase >= 2) { // Only checking game over when phase 2 has started
                checkGameOver();
            }
            
            // Switching the turn after removing the stone
            switchPlayer();
            if (ui != null) {
                ui.updateGameStatus("Turn: " + currentPlayer.getName());
            }
        } else {
            throw new InvalidMove("Cannot remove this stone.");
        }
    }
    
    /**
     * Helper method to remove a stone from the board and update the game state.
     *
     * @param node     the node from which the stone is to be removed.
     * @param opponent the player whose stone is being removed.
     */
    private void removeStone(Node node, Player opponent) {
        node.setOccupant(null);
        opponent.decrementStonesOnBoard();
        millFormed = false; // Reset flag after removal
        logger.log(Level.ALL, "HumanPlayer {0}'s stone at node {1} has been removed.", new Object[]{opponent.getName(), node.getId()});
        checkGameOver();
    }

    /**
     * Checks if the game is currently in the placing phase.
     *
     * @return true if the game is in the placing phase, false otherwise.
     */
    public boolean isPlacingPhase() {
        return phase == 1;
    }

    /**
     * Gets the current player whose turn it is.
     *
     * @return the current player.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Gets the game board.
     *
     * @return the board object representing the game board.
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Checks if the current player is allowed to "fly" (move a piece to any open space on the board).
     * A player can fly if they have exactly 3 stones on the board.
     *
     * @param currentPlayer the player whose ability to fly is being checked.
     * @return true if the player can fly, false otherwise.
     */
    public boolean canFly(Player currentPlayer) {
        logger.log(Level.INFO, "HumanPlayer {0} has {1} stones on board.", new Object[]{currentPlayer.getName(), currentPlayer.getStonesOnBoard()});
        boolean canFly = moveValidator.canFly(currentPlayer);  // HumanPlayer can fly if they have exactly 3 stones
        logger.log(Level.INFO, "HumanPlayer {0} canFly: {1}", new Object[]{currentPlayer.getName(), canFly});
        return canFly;
    }

    /**
     * Checks if the game should move to the next phase (from placing to moving).
     */
    private void checkPhase() {
        if (humanPlayer1.getStonesToPlace() == 0 && humanPlayer2.getStonesToPlace() == 0) {
            phase = 2;
        }
    }

    /**
     * Gets the current game phase.
     *
     * @return the current phase of the game.
     */
    public int getPhase() {
        return phase;
    }

    /**
     * Checks if a mill has been formed by the current player.
     *
     * @return true if a mill has been formed, false otherwise.
     */
    public boolean isMillFormed() {
        return millFormed;
    }

    /**
     * Sets the mill formed status.
     *
     * @param millFormed true if a mill has been formed, false otherwise.
     */
    public void setMillFormed(boolean millFormed) {
        this.millFormed = millFormed;
    }

    /**
     * Checks if the game is over and determines the winner if applicable.
     */
    private void checkGameOver() {
        if (phase >= 2) {
            // Check if any player has 2 or fewer stones
            if (humanPlayer1.getStonesOnBoard() <= 2) {
                gameOver(humanPlayer2);
                return;
            } else if (humanPlayer2.getStonesOnBoard() <= 2) {
                gameOver(humanPlayer1);
                return;
            }
        }
        // Check if any player has no valid moves left
        if (!board.hasValidMoves(humanPlayer1)) {
            gameOver(humanPlayer2);
            gameOver(humanPlayer1);
        }
    }

    /**
     * Ends the game and declares the winner.
     *
     * @param winner the player who won the game.
     */
    private void gameOver(Player winner) {
        logger.log(Level.INFO, "Game Over! {0} wins!", new Object[]{winner.getName()});
        if (ui != null) {
            ui.displayGameOverMessage(winner);  // Display the game-over message
        }
    }

    /**
     * Sets the UI reference for the game to interact with.
     *
     * @param ui the MillGameUI object to be used for updating the game status and UI.
     */
    public void setUI(MillGameUI ui) {
        this.ui = ui;
    }

}
