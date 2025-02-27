package MCTS;

import game.mills.Board;
import game.mills.Node;
import game.mills.Player;
import javafx.scene.paint.Color;

/**
 * MCTSPlayer implements a Monte Carlo Tree Search AI for the Mills game.
 */
public class MCTSPlayer implements Player {
    private final String name;
    private final Color color;
    private int stonesToPlace;
    private int stonesOnBoard;
    private MonteCarloBot mctsBot;

    public MCTSPlayer(String name, Color color) {
        this.name = name;
        this.color = color;
        this.stonesToPlace = 9; // Initial number of stones
        this.stonesOnBoard = 0;
        this.mctsBot = new MonteCarloBot();
    }

    @Override
    public String getName() { return name; }

    @Override
    public Color getColor() { return color; }

    @Override
    public int getStonesToPlace() { return stonesToPlace; }

    @Override
    public int getStonesOnBoard() { return stonesOnBoard; }

    @Override
    public void decrementStonesToPlace() { stonesToPlace--; }

    @Override
    public void incrementStonesOnBoard() { stonesOnBoard++; }

    @Override
    public void decrementStonesOnBoard() { stonesOnBoard--; }

    public void makeMove(Board board, Player opponent) {
        // Utiliser le MonteCarloBot pour décider du mouvement
        int move = mctsBot.placePiece(new GameWrapper(board, this, opponent));
        if (move != -1) {
            board.placePiece(this, move);
        }
    }
}