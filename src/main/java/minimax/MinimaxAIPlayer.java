package minimax;

import game.mills.*;
import gui.MillGameUI;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import javafx.application.Platform;
import neuralnetwork.BoardCNN;
import neuralnetwork.HybridEvaluator;

@Log
public class MinimaxAIPlayer implements Player {
    private final int depth;
    @Setter
    private MinimaxAlgorithm minimax;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private Color color;
    @Getter
    private int stonesToPlace;
    @Getter
    private int stonesOnBoard;
    @Setter
    private Game game;
    private List<String> boardStateHistory;
    private BoardCNN cnn;
    private HybridEvaluator hybridEvaluator;

    public MinimaxAIPlayer(String name, Color color, int depth, Game game) {
        this.name = name;
        this.color = color;
        this.game = game;
        this.depth = depth;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
        this.boardStateHistory = new ArrayList<>();
        EvaluationFunction evaluationFunction = new EvaluationFunction(game);
        this.minimax = new MinimaxAlgorithm(depth, evaluationFunction, game);
        this.cnn = new BoardCNN();
        this.hybridEvaluator = new HybridEvaluator(game);
    }

    public void makeMove(Board board, int phase) {
        Platform.runLater(() -> {
            if (stonesToPlace == 9) {
                Random r = new Random();
                int randomPlacement = r.nextInt(24);
                if (board.getNode(randomPlacement).isOccupied()) {
                    randomPlacement = r.nextInt(24);
                    game.placePiece(randomPlacement);
                    MillGameUI.incrementMinimaxMoves();
                } else {
                    game.placePiece(randomPlacement);
                    MillGameUI.incrementMinimaxMoves();
                }
            } else {
                if (phase == 1) {
                    int bestPlacement = minimax.findBestPlacement(board, this);
                    if (bestPlacement != -1) {
                        try {
                            game.placePiece(bestPlacement);
                            MillGameUI.incrementMinimaxMoves();
                            if (game.isMillFormed()) {
                                handleMillFormation(board);
                            }
                        } catch (InvalidMove e) {
                            log.log(Level.WARNING, "Failed to place piece: {0}", e.getMessage());
                        }
                    }
                } else {
                    Node[] bestMove = minimax.findBestMove(board, this, phase);
                    if (bestMove != null && bestMove[0] != null && bestMove[1] != null) {
                        try {
                            game.makeMove(bestMove[0].getId(), bestMove[1].getId());
                            MillGameUI.incrementMinimaxMoves();
                            if (game.isMillFormed()) {
                                handleMillFormation(board);
                            }
                        } catch (InvalidMove e) {
                            log.log(Level.WARNING, "Failed to make move: {0}", e.getMessage());
                        }
                    } else {
                        log.log(Level.WARNING, "No valid move found for AI.");
                        if (bestMove[0] == null || bestMove[1] == null) {
                            log.warning("No valid move found. Falling back to random.");
                            for (Node fromNode : board.getNodes().values()) {
                                if (fromNode.getOccupant() == this) {
                                    for (Node toNode : board.getNeighbours(fromNode)) {
                                        if (!toNode.isOccupied()) {
                                            game.makeMove(fromNode.getId(), toNode.getId());
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public boolean isRepeatedMove(Board board) {
        String currentState = getBoardStateString(board);
        if (boardStateHistory.contains(currentState)) {
            return true;
        } else {
            boardStateHistory.add(currentState);
            return false;
        }
    }

    private String getBoardStateString(Board board) {
        StringBuilder boardState = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            Node node = board.getNode(i);
            Player occupant = node.getOccupant();
            if (occupant == null) {
                boardState.append("0");
            } else if (occupant == this) {
                boardState.append("1");
            } else {
                boardState.append("2");
            }
        }
        return boardState.toString();
    }

    /**
     * Decreases the number of stones the AI player has to place by one
     * and increments the stones on the board. This is used during the placement phase.
     */
    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

    /**
     * Increases the number of stones the AI player has on the board by one.
     * This is called when a new stone is added to the board.
     */
    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    /**
     * Decreases the number of stones the AI player has on the board by one.
     * This is typically called when a stone is removed from the board.
     */
    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }

    // Helper method to handle mill formation and remove opponent's piece
    private void handleMillFormation(Board board) {
        Node bestRemovalNode = minimax.bestRemoval(board, this);
        if (bestRemovalNode != null) {
            try {
                game.removePiece(bestRemovalNode.getId());
                //log.log(Level.INFO, "AI removed opponent's piece at node {0}", new Object[]{bestRemovalNode.getId()});
            } catch (InvalidMove e) {
                log.log(Level.WARNING, "Failed to remove piece: {0}", e.getMessage());
            }
        } else {
            log.log(Level.WARNING, "No opponent pieces to remove.");
        }
    }

    public int evaluateBoard(Board board, Player player, int phase, Node node) {
        return hybridEvaluator.evaluate(board, player, phase, node);
    }

}
