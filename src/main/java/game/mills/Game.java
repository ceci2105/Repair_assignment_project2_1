package game.mills;

import java.util.logging.Level;
import java.util.logging.Logger;


import Minimax.MinimaxAIPlayer;

import agents.neural_network.BaselineAgent;

import gui.MillGameUI;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

/**
 * The NewGame class manages the game logic for Mills.
 * It controls the game phases, player turns, moves, and interactions with the game board.
 */
public class Game {
    private static Logger logger = Logger.getLogger(Game.class.getName());
    private Player humanPlayer1;
    private Player humanPlayer2;
    /**
     * -- GETTER --
     *  Gets the current player whose turn it is.
     *
     * @return the current player.
     */
    @Getter
    private Player currentPlayer;
    /**
     * -- GETTER --
     *  Gets the game board.
     *
     * @return the board object representing the game board.
     */
    @Getter
    private Board board;
    private MoveValidator moveValidator;
    @SuppressWarnings("unused")
    private int totalMoves;
    /**
     * -- GETTER --
     *  Gets the current game phase.
     *
     * @return the current phase of the game.
     */
    @Getter
    private int phase;
    /**
     * -- GETTER --
     *  Checks if a mill has been formed by the current player.
     *
     *
     * -- SETTER --
     *  Sets the mill formed status.
     *
     @return true if a mill has been formed, false otherwise.
      * @param millFormed true if a mill has been formed, false otherwise.
     */
    @Setter
    @Getter
    private boolean millFormed = false;
    private MillGameUI ui;

    public boolean isGameOver = false;

    private boolean movingPhaseMessageDisplayed = false;

    /**
     * Constructs a new game instance with two players.
     * Initializes the board and sets up the game for the placing phase.
     *
     * @param p1 the first player.
     * @param p2 the second player.
     */
    public Game(Player p1, Player p2) {
        this.humanPlayer1 = p1;
        this.humanPlayer2 = p2;
        this.currentPlayer = p1;
        this.board = new Board();
        this.moveValidator = new MoveValidator(board);
        this.phase = 1; //Start the game in the placing phase

        this.totalMoves = 0;

 



        if (p1 instanceof BaselineAgent) {
            ((BaselineAgent) p1).setGame(this);
        }
        if (p2 instanceof BaselineAgent) {
            ((BaselineAgent) p2).setGame(this);
        }
        if (p1 instanceof MinimaxAIPlayer) {
            ((MinimaxAIPlayer) p1).setGame(this);
        }
        if (p2 instanceof MinimaxAIPlayer) {
            ((MinimaxAIPlayer) p2).setGame(this);
        }
        //TODO: refactor into switch case

    }

    /**
     * Switches the player when the turn changes.
     */
    public void switchPlayer() {
        logger.log(Level.INFO, "PLayer switch called!");
        // if (currentPlayer == humanPlayer1 || currentPlayer == humanPlayer2) {
        //    currentPlayer = (currentPlayer == humanPlayer1) ? humanPlayer2 : humanPlayer1;
        //} else {
        //    currentPlayer = (currentPlayer == aiPlayer) ? humanPlayer1 : aiPlayer;
        // }
    


        currentPlayer = (currentPlayer == humanPlayer1) ? humanPlayer2 : humanPlayer1;
        if (currentPlayer instanceof BaselineAgent) {
            logger.log(Level.INFO, "Baseline Player");
            ((BaselineAgent) currentPlayer).makeMove();
        } else if (currentPlayer instanceof MinimaxAIPlayer) {
            logger.log(Level.INFO, "Minimax Player");
            ((MinimaxAIPlayer) currentPlayer).makeMove(board, phase);
        }
        notifyUI();
    }

    public Player getOpponent(Player player) {
        if (player == humanPlayer1) {
            return humanPlayer2;
        } else if (player == humanPlayer2) {
            return humanPlayer1;
        } else {
            throw new IllegalArgumentException("Unknown player: " + player);
        }
    }

    /**
     * Places a piece on the board at the specified node ID.
     * Validates the move and checks if a mill has been formed.
     *
     * @param nodeID the node ID where the piece is to be placed.
     * @throws InvalidMove if the move is not valid.
     */
    public void placePiece(int nodeID) {
        logger.log(Level.INFO, "Place Piece called");
        if (moveValidator.isValidPlacement(currentPlayer, nodeID)) {
            logger.log(Level.INFO, "If statement entered -> Valid placement");
            board.placePiece(currentPlayer, nodeID);
            notifyUI();
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
            notifyUI();
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
    public void removeStone(Node node, Player opponent) {
        node.setOccupant(null);
        opponent.decrementStonesOnBoard();
        millFormed = false; // Reset flag after removal
        logger.log(Level.ALL, "HumanPlayer {0}'s stone at node {1} has been removed.", new Object[]{opponent.getName(), node.getId()});
        checkGameOver();
    }

    /**
     * Removes a piece from the board at the specified node ID.
     * Validates the removal and updates the game state.
     *
     * @param nodeID the node ID where the piece is to be removed.
     * @throws InvalidMove if the removal is not valid.
     */
    public void removePiece(int nodeID) throws InvalidMove {
        Node node = board.getNode(nodeID);
        if (node.isOccupied() && node.getOccupant() != currentPlayer && !board.isPartOfMill(node)) {
            removeStone(node, node.getOccupant());
            notifyUI();
            switchPlayer();
        } else {
            throw new InvalidMove("Removal is invalid!");
        }
    }

    /**
     * Checks if the game is currently in the placing phase.
     *
     * @return true if the game is in the placing phase, false otherwise.
     */
    public boolean isPlacingPhase() {
        return phase == 1;
    }

    public void notifyUI() {
        if (ui != null) {
            ui.refreshBoard();
        }
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
            if (!movingPhaseMessageDisplayed) {
                if (ui != null) {
                    ui.updateGamePhaseLabel("Game is now in moving phase!");
                }
                movingPhaseMessageDisplayed = true;
            }
        }
    }

    /**
     * Checks if the game is over and determines the winner if applicable.
     */
    private void checkGameOver() {
        if (isGameOver) {
            return; // Do not proceed if the game is already over
        }
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
        boolean p1HasMoves = board.hasValidMoves(humanPlayer1);
        boolean p2HasMoves = board.hasValidMoves(humanPlayer2);

        if (!p1HasMoves && !p2HasMoves) {
            // If neither player has valid moves, it's a draw
            gameOver(null);
        } else if (!p1HasMoves) {
            gameOver(humanPlayer2);
        } else if (!p2HasMoves) {
            gameOver(humanPlayer1);
        }
    }

    /**
     * Ends the game and declares the winner.
     *
     * @param winner the player who won the game.
     */
    private void gameOver(Player winner) {
        if (!isGameOver) {
            isGameOver = true;
            logger.log(Level.INFO, winner != null ? "Game Over! {0} wins!" : "Game Over! It's a draw!", new Object[]{winner != null ? winner.getName() : ""});
            if (ui != null) {
                ui.displayGameOverMessage(winner);  // Display the game-over message
            }
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

    public void setSecondPlayer(Player player) {
        this.humanPlayer2 = player;
    }


}
