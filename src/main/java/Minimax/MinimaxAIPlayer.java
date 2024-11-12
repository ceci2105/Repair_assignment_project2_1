package Minimax;

import game.mills.Player;
import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import javafx.scene.paint.Color;


import java.awt.*;

public class MinimaxAIPlayer implements Player {
    private int depth;
    private MinimaxAlgorithm minimax;
    private String name;
    private Color color;
    private int stonesToPlace;
    private int stonesOnBoard;

    // Constructor that accepts a Game instance
    public MinimaxAIPlayer(String name, int depth, Game game) {
        this.name = name;
        this.depth = depth;
        this.minimax = new MinimaxAlgorithm(game, depth);
    }

    // Implement the abstract getName method from Player
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Color getColor() {
        return color;
    }

    // Executes the best move found by Minimax and returns the nodes involved in the move
    public void makeMove(Board board, int phase) {
        Node[] bestMove = minimax.findBestMove(board, this, phase);
        if (bestMove != null && bestMove[0] != null && bestMove[1] != null) {
            board.movePiece(this, bestMove[0].getId(), bestMove[1].getId());
        }
    }

    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

    public int getStonesToPlace() {
        return stonesToPlace;
    }

    public int getStonesOnBoard() {
        return stonesOnBoard;
    }

    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    /**
     * Decrements the number of stones the player has on the board by one.
     */
    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }


}
