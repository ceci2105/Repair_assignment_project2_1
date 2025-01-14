import game.mills.*;
import javafx.scene.paint.Color;

public class HybridAIPlayer implements Player {
    private MinimaxAlgorithm minimax;
    private NeuralNetwork neuralNetwork;
    private String name;
    private Color color;
    private int stonesToPlace;
    private int stonesOnBoard;
    private Game game;

    public HybridAIPlayer(String name, Color color, Game game, int depth) {
        this.name = name;
        this.color = color;
        this.game = game;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
        this.minimax = new MinimaxAlgorithm(game, depth);
        this.neuralNetwork = new NeuralNetwork();
    }

    // Implement Player interface methods
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public int getStonesToPlace() {
        return stonesToPlace;
    }

    @Override
    public int getStonesOnBoard() {
        return stonesOnBoard;
    }

    @Override
    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

    @Override
    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    @Override
    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }

    /**
     * Makes a move using the hybrid approach combining CNN evaluation with minimax
     * search
     */
    public void makeMove(Board board, int phase) {
        if (phase == 1) {
            int bestPlacement = findBestPlacement(board);
            if (bestPlacement != -1) {
                game.placePiece(bestPlacement);
                if (game.isMillFormed()) {
                    handleMillFormation(board);
                }
            }
        } else {
            Node[] bestMove = findBestMove(board, phase);
            if (bestMove != null && bestMove[0] != null && bestMove[1] != null) {
                game.makeMove(bestMove[0].getId(), bestMove[1].getId());
                if (game.isMillFormed()) {
                    handleMillFormation(board);
                }
            }
        }
    }

    private int findBestPlacement(Board board) {
        int bestPlacement = -1;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Node node : board.getNodes().values()) {
            if (!node.isOccupied()) {
                // Make the move on a copy of the board
                Board copyBoard = board.deepCopy();
                copyBoard.placePieceAgent(this, node.getId());

                // Combine minimax evaluation with neural network evaluation
                double minimaxValue = minimax.findBestPlacement(copyBoard, this);
                double neuralValue = neuralNetwork.evaluatePosition(copyBoard, this);

                // Weighted combination of both evaluations
                double combinedValue = 0.7 * minimaxValue + 0.3 * neuralValue;

                if (combinedValue > bestValue) {
                    bestValue = combinedValue;
                    bestPlacement = node.getId();
                }
            }
        }

        return bestPlacement;
    }

    private Node[] findBestMove(Board board, int phase) {
        Node[] bestMove = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Node fromNode : board.getNodes().values()) {
            if (fromNode.getOccupant() == this) {
                for (Node toNode : board.getNeighbours(fromNode)) {
                    if (!toNode.isOccupied() && board.isValidMove(fromNode, toNode)) {
                        // Make the move on a copy of the board
                        Board copyBoard = board.deepCopy();
                        copyBoard.movePiece(this, fromNode.getId(), toNode.getId());

                        // Combine minimax evaluation with neural network evaluation
                        Node[] minimaxMove = minimax.findBestMove(copyBoard, this, phase);
                        double neuralValue = neuralNetwork.evaluatePosition(copyBoard, this);

                        // If minimax found a valid move, combine evaluations
                        if (minimaxMove != null) {
                            double combinedValue = 0.7 * minimax.evaluatePosition(copyBoard) +
                                    0.3 * neuralValue;

                            if (combinedValue > bestValue) {
                                bestValue = combinedValue;
                                bestMove = new Node[] { fromNode, toNode };
                            }
                        }
                    }
                }
            }
        }

        return bestMove;
    }

}
