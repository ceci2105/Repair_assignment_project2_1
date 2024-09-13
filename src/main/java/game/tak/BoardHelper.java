package game.tak;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents static helper methods for the Board class.
 */
public class BoardHelper {

    /**
     * Checks if the position of the stone is valid.
     * @param row The row position of the position to be checked.
     * @param col The column of the position to be checked.
     * @param boardSize The size of the board.
     * @return True if the placement is valid, false otherwise.
     */
    protected static boolean isValidPosition(int row, int col, int boardSize) {
        return row >= 0 && row < boardSize && col >= 0 && col < boardSize;
    }

    /**
     * Checks if the player has won the game based on the current state of the game.
     * @param player The player to check.
     * @param board The game board.
     * @return True if the player wins, false otherwise.
     */
    protected static boolean checkForWin(Player player, Board board) {
        int boardSize = board.getSize();
        Set<Position> playerPos = new HashSet<>();
        // Collect all the pieces placed by the player:
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getOwner() == player && (piece.getType() == PieceType.FLAT || piece.getType() == PieceType.CAPSTONE)) {
                    playerPos.add(new Position(row, col));
                }
            }
        }

        // Check for a road connecting:
        // Top or bottom.
        for (Position startPosition : playerPos) {
            if (startPosition.row == 0) {
                for (Position endPosition : playerPos) {
                    if (endPosition.row == boardSize - 1 && isConnected(board.getGraph(), startPosition, endPosition)) {
                        return true;
                    }
                }
            }
        }

        // Check for left and right sides.
        for (Position startPosition : playerPos) {
            if (startPosition.col == 0) {
                for (Position endPosition : playerPos) {
                    if (endPosition.col == boardSize - 1 && isConnected(board.getGraph(), startPosition, endPosition)) ;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the graph has a path between two positions.
     * @param graph The graph to be checked.
     * @param startPosition The start position to check.
     * @param endPosition The end position to be checked.
     * @return True if the graph is connected, false otherwise.
     */
    private static boolean isConnected(Graph graph, Position startPosition, Position endPosition) {
        ConnectivityInspector<Position, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(graph);
        return connectivityInspector.pathExists(startPosition, endPosition);
    }

    /**
     * Checks if a potential move is a valid move.
     * @param board The game board.
     * @param startRow The start row of the move.
     * @param startCol The start column of the move.
     * @param endRow The row position after the move.
     * @param endCol The column position after the move.
     * @param numPiecesToMove The number of pieces to be moved.
     * @return True if the move is valid, false otherwise.
     */
    protected static boolean isValidMove(Board board, int startRow, int startCol, int endRow, int endCol, int numPiecesToMove) {
        /*
        Logic:
        - Are the start and end positions valid
        - Is there a stack at the start position
        - Is the number of pieces to move valid (not more than the stack height)
        - Is the move direction valid (orthogonal or diagonal)
        - Is the path clear (no obstacles or illegal stacking)
         */

        if (!isValidPosition(startRow, startCol, board.getSize()) || !isValidPosition(endRow, endCol, board.getSize())) {
            return false;
        }

        if (board.getPieceAt(startRow,startCol) == null) {
            return false;
        }

        int stackHeight = getStackHeight(board, startRow, startCol);

        if (numPiecesToMove <= 0 || numPiecesToMove > stackHeight) {
            return false;
        }

        if (!isOrthogonal(startRow, startCol, endRow, endCol)) {
            return false;
        }

        if (!isPathClear(board, startRow, startCol, endRow, endCol, numPiecesToMove)) {
            return false;
        }

        return true;
    }

    /**
     * Gets all the pieces in a given path.
     * @param board The game board.
     * @param startRow The starting row of the path.
     * @param startCol The starting column of the path.
     * @param endRow The end row of the path.
     * @param endCol The end column of the path.
     * @return a List containing the pieces in the given path.
     */
    protected static List<Piece> getPiecesInPath(Board board, int startRow, int startCol, int endRow, int endCol) {
        List<Piece> piecesInPath = new ArrayList<>();

        // Determine the move direction:
        int rowIncrement = Integer.compare(endRow, startRow);
        int colIncrement = Integer.compare(endCol, startCol);

        // Collect pieces
        int currentRow = startRow;
        int currentCol = startCol;

        while (currentRow != endRow || currentCol != endCol) {
            if (board.getPieceAt(currentRow,currentCol) != null) {
                piecesInPath.add(board.getPieceAt(currentRow,currentCol));
            }
            currentRow += rowIncrement;
            currentCol += colIncrement;
        }

        // Add a piece at end position (if possible or if there are any)
        if (board.getPieceAt(endRow, endCol) != null) {
            piecesInPath.add(board.getPieceAt(endRow,endCol));
        }

        return piecesInPath;
    }

    /**
     * Gets the height of the Stack of Pieces at a given position
     * @param board The game board.
     * @param row The row to be checked.
     * @param col The column to be checked.
     * @return The height of the stack at the given position.
     */
    private static int getStackHeight(Board board, int row, int col) {
        int height = 0;
        while (isValidPosition(row, col, board.getSize()) && board.getPieceAt(row, col) != null) {
            height++;
            row++;
        }
        return height;
    }

    /**
     * Checks if two positions are orthogonal.
     * @param startRow The row of the starting position.
     * @param startCol The column of the starting column.
     * @param endRow The row at the destination.
     * @param endCol The column at the destination.
     * @return True if it's orthogonal, False otherwise.
     */
    private static boolean isOrthogonal(int startRow, int startCol, int endRow, int endCol) {
        return (startRow == endRow && startCol != endCol) || (startRow != endRow && startCol == endCol);
    }

    /**
     * Checks if the given path is clear.
     * @param board The game board.
     * @param startRow The starting row of the path.
     * @param startCol The starting column of the path.
     * @param endRow The end row of the path.
     * @param endCol The end column of the path.
     * @param numPiecesToMove The number of pieces to be moved.
     * @return True, if the path is clear, false otherwise.
     */
    private static boolean isPathClear(Board board, int startRow, int startCol, int endRow, int endCol, int numPiecesToMove){
        int rowIncrement = Integer.compare(endRow, startRow);
        int colIncrement = Integer.compare(endCol, startCol);

        int currentRow = startRow + rowIncrement;
        int currentCol = startCol + colIncrement;

        while (currentRow != endRow || currentCol != endCol) {
            // Check for obstacles
            if (board.getPieceAt(currentRow, currentCol) != null) {
                return false;
            }
            // Check for illegal stacking (if dropping more than 1 piece)
            if (numPiecesToMove > 1) {
                Piece prevPiece = board.getPieceAt(currentRow - rowIncrement, currentCol - colIncrement);
                Piece endPiece = board.getPieceAt(endRow, endCol);
                if (prevPiece.getType() == PieceType.STANDING && (endPiece != null && !(endPiece.getType() == PieceType.STANDING))) {
                    return false;
                }
            }
        }
        return true;
}

    /**
     * Logic to capture a piece.
     * @param board The game board.
     * @param capturingPlayer The player capturing a piece.
     * @param capturedPieces A list containing all the pieced captured by the player.
     */
    protected static void capturePieces(Board board, Player capturingPlayer, List<Piece> capturedPieces) {
        /*
         Remove captured pieces from the board
         Add captured pieces to the capturing players collection
         */
        for (Piece capturedPiece : capturedPieces) {
            Position capturedPos = findPiecePosition(board, capturedPiece);
            board.placePiece(null, capturedPos.row, capturedPos.col);
            capturingPlayer.addCapturedPiece(capturedPiece);
        }
    }

    /**
     * Finds the position of a given piece.
     * @param board The game board.
     * @param pieceToFind The piece that should be found.
     * @return The position of the piece.
     */
    private static Position findPiecePosition(Board board, Piece pieceToFind) {
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                if (board.getPieceAt(row, col) == pieceToFind) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }
}
