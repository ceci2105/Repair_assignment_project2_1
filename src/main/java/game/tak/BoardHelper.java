package game.tak;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;

public class BoardHelper {

    protected static boolean isValidPosition(int row, int col, int boardSize) {
        return row >= 0 && row < boardSize && col >= 0 && col < boardSize;
    }

    protected static boolean checkForWin(Player player, Graph<Position, DefaultEdge> graph, int boardSize) {
        // TODO: finish implementation.
        return false;
    }

    protected static boolean isValidMove(Piece[][] board, int startRow, int startCol, int endRow, int endCol, int numPiecesToMove) {
        /*
        Logic:
        - Are the start and end positions valid
        - Is there a stack at the start position
        - Is the number of pieces to move valid (not more than the stack height)
        - Is the move direction valid (orthogonal or diagonal)
        - Is the path clear (no obstacles or illegal stacking)
         */

        if (!isValidPosition(startRow, startCol, board.length) || !isValidPosition(endRow, endCol, board.length)) {
            return false;
        }

        if (board[startRow][startCol] == null) {
            return false;
        }

        int stackHeight = getStackHeight(board, startRow, startCol);

        if (numPiecesToMove <= 0 || numPiecesToMove > stackHeight) {
            return false;
        }

        return isOrthogonal(startRow, startCol, endRow, endCol);

        // if (!isPathClear(board, startRow, startCol, endRow, endCol, numPiecesToMove)) {
        //     return false;
        // }
    }

    protected static List<Piece> getPiecesInPath(Piece[][] board, int startRow, int startCol, int endRow, int endCol) {
        List<Piece> piecesInPath = new ArrayList<>();

        // Determine the move direction:
        int rowIncrement = Integer.compare(endRow, startRow);
        int colIncrement = Integer.compare(endCol, startCol);

        // Collect pieces
        int currentRow = startRow;
        int currentCol = startCol;

        while (currentRow != endRow || currentCol != endCol) {
            if (board[currentRow][currentCol] != null) {
                piecesInPath.add(board[currentRow][currentCol]);
            }
            currentRow += rowIncrement;
            currentCol += colIncrement;
        }

        // Add piece at end position (if possible or if there are any)
        if (board[endRow][endCol] != null) {
            piecesInPath.add(board[endRow][endCol]);
        }

        return piecesInPath;
    }

    private static int getStackHeight(Piece[][] board, int row, int col) {
        int height = 0;
        while (isValidPosition(row, col, board.length) && board[row][col] != null) {
            height++;
            row++;
        }
        return height;
    }

    private static boolean isOrthogonal(int startRow, int startCol, int endRow, int endCol) {
        return (startRow == endRow && startCol != endCol) || (startRow != endRow && startCol == endCol);
    }

    /*
    private static boolean isPathClear(Piece[][] board, int startRow, int startCol, int endRow, int endCol, int numPiecesToMove) {
        int rowIncrement = Integer.compare(endRow, startRow);
        int colIncrement = Integer.compare(endCol, startCol);

        int currentRow = startRow + rowIncrement;
        int currentCol = startCol + colIncrement;

        while (currentRow != endRow || currentCol != endCol) {
            if (board[currentRow][currentCol] != null) {
                return false;
            }

            if (numPiecesToMove > 1 &&
                    board[currentRow - rowIncrement][currentCol - colIncrement] instanceof StandingStone &&
                    !(board[currentRow][currentCol] instanceof StandingStone)) {
                return false;
            }

            currentRow += rowIncrement;
            currentCol += colIncrement;
        }

        if (numPiecesToMove > 1 &&
                board[currentRow - rowIncrement][currentCol - colIncrement] instanceof StandingStone &&
                !(board[currentRow][currentCol] instanceof StandingStone)) {
            return false;
        }
        return true;
    }

     */

    protected static void capturePieces(Player capturingPlayer, List<Piece> capturedPieces) {
        /*
         Remove captured pieces from the board
         Add captured pieces to the capturing players collection
         */
        // TODO: finish implementation.
    }
}
