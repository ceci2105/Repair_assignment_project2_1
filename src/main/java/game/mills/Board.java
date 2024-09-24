package game.mills;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;

public class Board extends GridPane {
    
    private final int size = 7; // The standard mill board is 7x7
    private final Circle[][] circles = new Circle[size][size];
    private ArrayList<Line> lines = new ArrayList<>();
    private boolean[][] validPositions = new boolean[size][size]; // Mark valid positions on the board
    
    private Player currentPlayer;
    private Player player1;
    private Player player2;

    public Board(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1;
        initializeBoard();
        setValidPositions();
    }

    private void initializeBoard() {
        // Create 7x7 grid for the board layout
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Circle circle = new Circle(15);
                circle.setStroke(Color.BLACK);
                circle.setFill(Color.WHITE);
                circles[row][col] = circle;
                add(circle, col, row);
    
                // Create final variables for use in lambda expression
                final int finalRow = row;
                final int finalCol = col;
    
                // Add click event for each position
                circle.setOnMouseClicked(event -> handleClick(finalRow, finalCol));
            }
        }
    
        drawMillLines();
    }

    private void setValidPositions() {
        // Set valid positions for Mills game (corresponds to where pieces can be placed)
        validPositions = new boolean[][] {
            { true, false, false, true, false, false, true },
            { false, true, false, true, false, true, false },
            { false, false, true, true, true, false, false },
            { true, true, true, false, true, true, true },
            { false, false, true, true, true, false, false },
            { false, true, false, true, false, true, false },
            { true, false, false, true, false, false, true }
        };
    }

    private void drawMillLines() {
        // Drawing the lines that connect the valid positions for the Mills game
        // This should ideally match the valid positions and the game board layout
        
        // Outer square
        lines.add(new Line(0, 0, 6, 0));  // Top Horizontal Line
        lines.add(new Line(0, 6, 6, 6));  // Bottom Horizontal Line
        lines.add(new Line(0, 0, 0, 6));  // Left Vertical Line
        lines.add(new Line(6, 0, 6, 6));  // Right Vertical Line

        // Middle square
        lines.add(new Line(1, 1, 5, 1));
        lines.add(new Line(1, 5, 5, 5));
        lines.add(new Line(1, 1, 1, 5));
        lines.add(new Line(5, 1, 5, 5));

        // Inner square
        lines.add(new Line(2, 2, 4, 2));
        lines.add(new Line(2, 4, 4, 4));
        lines.add(new Line(2, 2, 2, 4));
        lines.add(new Line(4, 2, 4, 4));

        // Connectors
        lines.add(new Line(0, 3, 2, 3)); // Left middle connector
        lines.add(new Line(4, 3, 6, 3)); // Right middle connector
        lines.add(new Line(3, 0, 3, 2)); // Top middle connector
        lines.add(new Line(3, 4, 3, 6)); // Bottom middle connector

        // Add all lines to the board
        for (Line line : lines) {
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2);
            add(line, GridPane.getColumnIndex(line), GridPane.getRowIndex(line));
        }
    }

    private void handleClick(int row, int col) {
        // Handle player click on a valid position
        if (!validPositions[row][col]) {
            return; // Invalid position
        }
        
        if (isOccupied(row, col)) {
            return; // Position already taken
        }

        placePiece(row, col, currentPlayer);

        if (checkMill(row, col, currentPlayer)) {
            System.out.println(currentPlayer.getName() + " formed a mill!");
            // Add further logic if needed (e.g., removing an opponent's piece)
        }

        // Switch players
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private boolean isOccupied(int row, int col) {
        return !circles[row][col].getFill().equals(Color.WHITE);
    }

    private void placePiece(int row, int col, Player player) {
        circles[row][col].setFill(player.getColor());
    }

    private boolean checkMill(int row, int col, Player player) {
        // Simple mill checking logic (should check rows, columns, and diagonals)

        // Check horizontal mill
        if (row == 0 || row == 3 || row == 6) {
            if (checkLine(row, 0, row, 3, row, 6, player)) return true;
        } else if (row == 1 || row == 5) {
            if (checkLine(row, 1, row, 3, row, 5, player)) return true;
        } else if (row == 2 || row == 4) {
            if (checkLine(row, 2, row, 3, row, 4, player)) return true;
        }

        // Check vertical mill
        if (col == 0 || col == 3 || col == 6) {
            if (checkLine(0, col, 3, col, 6, col, player)) return true;
        } else if (col == 1 || col == 5) {
            if (checkLine(1, col, 3, col, 5, col, player)) return true;
        } else if (col == 2 || col == 4) {
            if (checkLine(2, col, 3, col, 4, col, player)) return true;
        }

        return false;
    }

    private boolean checkLine(int r1, int c1, int r2, int c2, int r3, int c3, Player player) {
        return circles[r1][c1].getFill() == player.getColor() &&
               circles[r2][c2].getFill() == player.getColor() &&
               circles[r3][c3].getFill() == player.getColor();
    }
}
