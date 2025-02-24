package game.mills;

import agents.neural_network.BaselineAgent;
import gui.MillGameUI;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import minimax.MinimaxAIPlayer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import MCTS.MCTSPlayer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * The NewGame class manages the game logic for Mills.
 * It controls the game phases, player turns, moves, and interactions with the
 * game board.
 */
@Slf4j
public class Game {
    @Getter
    private final Map<INDArray, INDArray> boardStates;
    private final HashMap<String, Integer> boardStateCount; // Track occurrences of board states
    private final String lastBoardState = null; // Last observed board state
    private final int consecutiveRepetitionCount = 0; // Count of consecutive repetitions
    @Getter
    private final Board board;
    private final MoveValidator moveValidator;
    public boolean isGameOver = false;
    @Setter
    @Getter
    private Player humanPlayer1;
    @Setter
    @Getter
    private Player humanPlayer2;
    @Getter
    private Player winner = null;
    @Setter
    private MoveCallback moveCallback;
    @Getter
    @Setter
    private Player currentPlayer;
    private int totalMoves;
    @Getter
    private int phase;
    @Setter
    @Getter
    private boolean millFormed = false;
    private MillGameUI ui;
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
        this.boardStates = new HashMap<>();
        this.board = new Board();
        this.moveValidator = new MoveValidator(board);
        this.phase = 1; // Start the game in the placing phase

        this.totalMoves = 0;
        this.boardStateCount = new HashMap<>();

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
        if (p1 instanceof MCTSPlayer) {
            ((MCTSPlayer) p1).setGame(this);
        }
        if (p2 instanceof MCTSPlayer) {
            ((MCTSPlayer) p2).setGame(this);
        }

    }


    /**
     * Helper method to notify the callback of game state changes.
     * This should be called after any move that changes the game state.
     */
    private void notifyMoveCallback() {
        if (moveCallback != null) {
            moveCallback.onMove(board, currentPlayer);
        }
    }

    /**
     * Switches the player when the turn changes.
     */
    public void switchPlayer() {
        if (isGameOver) {
            return; // Do not switch player or make AI move if game is over
        }
        currentPlayer = (currentPlayer == humanPlayer1) ? humanPlayer2 : humanPlayer1;

        if (currentPlayer instanceof BaselineAgent || currentPlayer instanceof MinimaxAIPlayer || currentPlayer instanceof MCTSPlayer) {
            Task<Void> aiTask = new Task<Void>() {
                @Override
                protected Void call() {
                    if (currentPlayer instanceof BaselineAgent) {
                        ((BaselineAgent) currentPlayer).makeMove();
                    } else if (currentPlayer instanceof MinimaxAIPlayer) {
                        ((MinimaxAIPlayer) currentPlayer).makeMove(board, phase);
                    }else if (currentPlayer instanceof MCTSPlayer) {
                        ((MCTSPlayer) currentPlayer).makeMove(board, getOpponent(currentPlayer));
                    }
                    return null;
                }
            };

            aiTask.setOnSucceeded(event -> Platform.runLater(() -> {
                notifyUI();
                if (ui != null) {
                    ui.updateGameStatus("Turn: " + getCurrentPlayer().getName());
                }
            }));

            aiTask.setOnFailed(event -> {
                Throwable error = aiTask.getException();
                log.error("Error in AI Computation!", error);
            });

            new Thread(aiTask).start();
        } else {
            notifyUI();
        }
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

    private void recordBoardState() {
        INDArray winnerState = Nd4j.zeros(1, 24);
        int i = 0;
        for (Node node : board.getNodes().values()) {
            if (!node.isOccupied()) {
                break;
            }
            if (winner.equals(null)) {
                winnerState.putScalar(i, 0);
            } else if (winner.equals(humanPlayer1) && node.getOccupant().equals(humanPlayer1)) {
                winnerState.putScalar(i, 1);
            } else if (winner.equals(humanPlayer2) && node.getOccupant().equals(humanPlayer2)) {
                winnerState.putScalar(i, 2);
            }
            i++;
        }
        boardStates.put(winnerState, boardToINDArray(board));
        log.info("Board state was recorded!");
    }

    public INDArray boardToINDArray(Board board) {
        int numNodes = 24;
        INDArray boardArray = Nd4j.zeros(1, numNodes);

        Map<Integer, Node> nodes = board.getNodes();
        for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
            int nodeId = entry.getKey();
            Node node = entry.getValue();
            Player occupant = node.getOccupant();
            if (occupant != null) {
                if (occupant.equals(this.humanPlayer1)) {
                    boardArray.putScalar(nodeId, 1);
                } else if (occupant.equals(this.humanPlayer2)) {
                    boardArray.putScalar(nodeId, 2);
                } else {
                    boardArray.putScalar(nodeId, 0);
                }
            }
        }
        System.out.println(boardArray);
        return boardArray;
    }

    private void saveBoardStates() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String filename = now.format(formatter) + ".dat";
        String directory = "Data";
        String filePath = Paths.get(directory, filename).toString();
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(filePath)))) {
            oos.writeObject(boardStates);
        } catch (IOException e) {
            log.error("Something went wrong saving the board state!", e);
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
        if (moveValidator.isValidPlacement(currentPlayer, nodeID)) {
            board.placePiece(currentPlayer, nodeID);
            trackBoardState();
            notifyUI();
            if (board.checkMill(board.getNode(nodeID), currentPlayer)) {
                millFormed = true;
            } else {
                switchPlayer();
            }
            totalMoves++;
            checkPhase();
            notifyMoveCallback();

        } else {
            throw new InvalidMove("Placement is invalid!");
        }
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
            trackBoardState();
            notifyUI();
            if (board.checkMill(board.getNode(toID), currentPlayer)) {
                millFormed = true;
            } else {
                switchPlayer();
            }
            checkGameOver();
            notifyMoveCallback();

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
            notifyMoveCallback();

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
     * Checks if the current player is allowed to "fly" (move a piece to any open
     * space on the board).
     * A player can fly if they have exactly 3 stones on the board.
     *
     * @param currentPlayer the player whose ability to fly is being checked.
     * @return true if the player can fly, false otherwise.
     */
    public boolean canFly(Player currentPlayer) {
        // HumanPlayer can fly if they have exactly 3 stones
        return moveValidator.canFly(currentPlayer);
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
            this.winner = winner;
            log.info(winner != null ? "Game Over! {0} wins!" : "Game Over! It's a draw!", winner != null ? winner.getName() : "");
            if (ui != null) {
                recordBoardState();
                saveBoardStates();
                ui.displayGameOverMessage(winner); // Display the game-over message
            }
        }
    }

    /**
     * Sets the UI reference for the game to interact with.
     *
     * @param ui the MillGameUI object to be used for updating the game status and
     *           UI.
     */
    public void setUI(MillGameUI ui) {
        this.ui = ui;
    }

    public void setSecondPlayer(Player player) {
        this.humanPlayer2 = player;
    }

    // In Game.java

    public void startGame() {
        boardStateCount.clear();
        if (currentPlayer instanceof BaselineAgent) {
            log.info("Starting game with Baseline Agent");
            // Add a delay before the bot makes its move
            PauseTransition pause = new PauseTransition(Duration.seconds(0.1)); // 0.1-second delay
            pause.setOnFinished(event -> {
                ((BaselineAgent) currentPlayer).makeMove();

                // Update the UI after the bot has made its move
                notifyUI();
                if (ui != null) {
                    ui.updateGameStatus("Turn: " + getCurrentPlayer().getName());
                }
            });
            pause.play();
        } else if (currentPlayer instanceof MinimaxAIPlayer) {
            log.info("Starting game with Minimax Agent");
            PauseTransition pause = new PauseTransition(Duration.seconds(0.1)); // 0.1-second delay
            pause.setOnFinished(event -> {
                ((MinimaxAIPlayer) currentPlayer).makeMove(board, phase);
                notifyUI();
                if (ui != null) {
                    ui.updateGameStatus("Turn: " + getCurrentPlayer().getName());
                }
            });
            pause.play();
        }
    }

    private void trackBoardState() {
        // Generate a unique representation of the current board state
        String boardStateHash = board.toString();

        // Update the board state count in the hashmap
        boardStateCount.put(boardStateHash, boardStateCount.getOrDefault(boardStateHash, 0) + 1);

        // Log the board state
//        logger.log(Level.INFO, "Board State: {0}, Count: {1}",
//                   new Object[] { boardStateHash, boardStateCount.get(boardStateHash) });

        // Check if this state has been repeated more than 2 times
        if (boardStateCount.get(boardStateHash) > 2) {
            log.info("Detected repetition loop, game is a draw");
            gameOver(null); // Call gameOver with null to indicate a draw
        }
    }


}
