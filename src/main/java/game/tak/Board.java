package game.tak;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static game.tak.BoardHelper.*;

/**
 * This class represents the Board itself. It contains important game logic and is supported by the BoardHelper class.
 */
public class Board {

    private final Piece[][] gameBoard; //The game board.
    private final int size; // The size of the game board.
    private final Graph<Position, DefaultEdge> graph; // The graph spanning the game board.
    public boolean firstTurn; // Flag to check if it's the first turn

    /**
     * Class constructor.
     *
     * @param size The size of the game board.
     */
    public Board(int size) {
        this.size = size;
        this.gameBoard = new Piece[size][size];
        this.graph = new SimpleGraph<>(DefaultEdge.class);
        this.firstTurn = true;
        initialiseEdges();
        initaliseVertices();
    }

    public int getSize() {
        return this.size;
    }

    public void setFirstTurn(boolean flag) {
        firstTurn = flag;
    }

    public Graph getGraph() {
        return graph;
    }

    /**
     * Small helper function to make the constructor smaller.
     */
    private void initaliseVertices() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                graph.addVertex(new Position(row, col));
            }
        }
    }

    /**
     * Small helper function to make the constructor smaller.
     */
    private void initialiseEdges() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                addEdgeForPosition(row, col);
            }
        }
    }

    /**
     * Helper function to initialise edges for a given position.
     *
     * @param row The row position of the position.
     * @param col The column position of the position.
     */
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

    /**
     * Gets a piece at a given position.
     *
     * @param row The row position to get the piece from.
     * @param col The column position to get the piece from.
     * @return The piece at the position. Throws an exception otherwise.
     */
    public Piece getPieceAt(int row, int col) {
        if (isValidPosition(row, col, size)) {
            return gameBoard[row][col];
        } else {
            throw new PieceDoesNotExist("This piece does nto exist!");
        }
    }

    /**
     * Function to place a piece on the game board.
     *
     * @param piece The piece to be placed.
     * @param row   The row index of the piece to be placed.
     * @param col   The column index of the piece to be placed.
     */
    public void placePiece(Piece piece, int row, int col) {
        if (!isValidPosition(row, col, size) || gameBoard[row][col] != null) {
            throw new InvalidPlacement("Can't place piece here!");
        }
        if (firstTurn && (piece.getType() != PieceType.FLAT)) {
            throw new WrongStoneType("First turn must be a Flat stone");
        }
        gameBoard[row][col] = piece;

        if (piece.isCapStone()) {
            updateGraphAfterCapstonePlacement(row, col);
        }
        setFirstTurn(false);

        if (checkForWin(piece.getOwner(), this)) {
            System.out.println("Game finished " + piece.getOwner() + " won");
        }

        // TODO: Implement game logic (changing turn etc)

    }

    /**
     * Updated the graph, after a capstone is placed.
     *
     * @param row The row position of the capstone.
     * @param col The column position of the capstone.
     */
    private void updateGraphAfterCapstonePlacement(int row, int col) {
        Position capstonePos = new Position(row, col);
        graph.removeAllEdges(graph.edgesOf(capstonePos));
    }

    /**
     * Moves a given stack.
     *
     * @param startRow        The start row.
     * @param startCol        The start column.
     * @param endRow          The end row.
     * @param endCol          The end column.
     * @param numPiecedToMove The number of pieces to be moved.
     * @param currentPlayer   The current player.
     */
    public void moveStack(int startRow, int startCol, int endRow, int endCol, int numPiecedToMove, Player currentPlayer) {
        if (!isValidMove(this, startRow, startCol, endRow, endCol, numPiecedToMove)) {
            throw new InvalidPlacement("Stack cant be moved there!");
        }

        Stack<Piece> stack = getStackAt(startRow, startCol);
        List<Piece> piecesToMove = new ArrayList<>(stack.subList(stack.size() - numPiecedToMove, stack.size()));

        for (int i = 0; i < numPiecedToMove; i++) {
            stack.pop();
        }

        List<Piece> piecesInPath = getPiecesInPath(this, startRow, startCol, endRow, endCol);
        List<Piece> capturedPieces = identifyCapturedPieces(piecesToMove, piecesInPath); // TODO: this method.

        if (!capturedPieces.isEmpty()) {
            capturePieces(this, currentPlayer, capturedPieces);
            for (Piece piece : capturedPieces) {
                if (piece.getType() == PieceType.CAPSTONE) {
                    updateGraphAfterCapstonePlacement(endRow, endCol);
                }
            }
        }

        int currentRow = endRow;
        int currentCol = endCol;
        for (int i = piecesToMove.size() - 1; i >= 0; i--) {
            placePiece(piecesToMove.get(i), currentRow, currentCol);

            if (endRow != startRow) {
                currentRow -= Integer.compare(endRow, startRow);
            } else {
                currentCol -= Integer.compare(endCol, startCol);
            }
        }
        // TODO: Implement function with this signature: updateGraphAfterMove(this, startRow, startCol, endRow, endCol);

        if (checkForWin(currentPlayer, this)) {
            System.out.println("Game finished " + currentPlayer + " won");
        }

        // TODO: Switch turn logic in Game.java!
    }

    /**
     * Gets a stack at a given position.
     *
     * @param startRow The row index of the position.
     * @param startCol The column index of the position.
     * @return The stack at the position, an exception otherwise.
     */
    protected Stack<Piece> getStackAt(int startRow, int startCol) {
        Stack<Piece> stack = new Stack<>();
        int currentRow = startRow;
        while (isValidPosition(currentRow, startCol, size) && gameBoard[currentRow][startCol] != null) {
            stack.push(gameBoard[currentRow][startCol]);
            currentRow++;
        }
        return stack;
    }

    /**
     * Function to identify which pieces are captured with a move.
     * @param movingPieces The pieced that move / are moved
     * @param piecesInPath The pieces in the given moving path.
     * @return The list of pieced captured.
     */
    private List<Piece> identifyCapturedPieces(List<Piece> movingPieces, List<Piece> piecesInPath) {
        List<Piece> capturedPieces = new ArrayList<>();

        Piece topMovingPiece = movingPieces.get(movingPieces.size() - 1);

        for (Piece pieceInPath : piecesInPath) {
            if (pieceInPath.getOwner() != topMovingPiece.getOwner()) {
                if (topMovingPiece.isCapStone()) {
                    capturedPieces.add(pieceInPath);
                } else if (topMovingPiece.getType() == PieceType.STANDING) {
                    int row = findPiecePosition(this, pieceInPath).row;
                    int col = findPiecePosition(this, pieceInPath).col;
                    capturedPieces.addAll(getStackAt(row, col));
                } else {
                    capturedPieces.add(pieceInPath);
                    break;
                }
            }
        }
        return capturedPieces;
    }

}
