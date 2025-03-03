package MCTS;

import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
import javafx.scene.paint.Color;

/**
 * MCTSPlayer implements a Monte Carlo Tree Search AI for the Mills game.
 */
public class MCTSPlayer implements Player {
    private Game game;
    private final String name;
    private final Color color;
    private int stonesToPlace;
    private int stonesOnBoard;
    private static final int SIMULATION_COUNT = 100;

    public MCTSPlayer(String name, Color color) {
        this.name = name;
        this.color = color;
        this.stonesToPlace = 9; // Initial number of stones
        this.stonesOnBoard = 0;
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

     public void setGame(Game game) {
        this.game = game;
    }

    public void makeMove(Board board, Player opponent) {
        Node bestMove = runMCTS(board, opponent);
        if (bestMove != null) {
            board.placePiece(this, bestMove.getId());
            this.decrementStonesToPlace(); 
            this.incrementStonesOnBoard(); 
        }
    }

    private Node runMCTS(Board board, Player opponent) {
        MCTSNode root = new MCTSNode(board.deepCopy(), this, opponent);
        System.out.println("Starting MCTS simulation...");

        for (int i = 0; i < SIMULATION_COUNT; i++) {
            MCTSNode selectedNode = root.select();
            selectedNode.expand();
            Player winner = selectedNode.simulate();
            selectedNode.backpropagate(winner);
        }
        
        return root.getBestMove();
    }
}