package game.tak;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import static game.tak.BoardHelper.*;
public class Board {
    private final Piece[][] gameBoard;
    private final int size;
    private final Graph<Position, DefaultEdge> graph;

    public Board(int size) {
        this.size = size;
        this.gameBoard = new Piece[size][size];
        this.graph = new SimpleGraph<>(DefaultEdge.class);
        initialiseEdges();
        initaliseVertices();
    }

    private void initaliseVertices() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                graph.addVertex(new Position(row, col));
            }
        }
    }

    private void initialiseEdges() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                addEdgeForPosition(row, col);
            }
        }
    }

    private void addEdgeForPosition(int row, int col) {

        if (row > 0) {
            graph.addEdge(new Position(row, col), new Position(row - 1, col));
            if (row < size - 1) {
                graph.addEdge(new Position(row, col), new Position(row + 1, col));
            }
            if (col > 0) {
                graph.addEdge(new Position(row, col), new Position(row, col - 1));
            }
            if (col < size - 1) {
                graph.addEdge(new Position(row, col), new Position(row, col + 1));
            }
        }
    }

    /*
    public void placePiece(Piece piece, int row, int col) {
        if (!isValidPosition(row, col, size)) ||gameBoard[row][col] != null) {
            throw new InvalidPlacement("Placement is not valid");
        }


    }
     */
}
