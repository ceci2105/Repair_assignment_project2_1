// Game.java
package game.mills;

public class Game {
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private BoardGraph board;
    private int totalMoves;
    private int phase; // 1: Placing, 2: Moving, 3: Flying

    public Game(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1;  // Player 1 starts the game
        this.board = new BoardGraph();  // Initialize the board graph
        this.totalMoves = 0;
        this.phase = 1;
    }

    // Method to get the board (graph) representation
    public BoardGraph getBoard() {
        return this.board;
    }

    public void placePiece(int nodeId) {
        Node node = board.getNode(nodeId);

        if (currentPlayer.getStonesToPlace() <= 0) {
            System.out.println(currentPlayer.getName() + " has no stones left to place.");
            return;
        }

        if (!node.isOccupied()) {
            node.setOccupant(currentPlayer);
            currentPlayer.decrementStonesToPlace();
            totalMoves++;

            // Check for mill formation
            if (board.checkMill(node, currentPlayer)) {
                System.out.println(currentPlayer.getName() + " has formed a mill!");
                // Implement logic to remove an opponent's piece
            }

            // Switch players
            switchPlayer();

            // Check if moving phase should start
            if (totalMoves >= 18) {
                phase = 2;
                System.out.println("All stones placed. Moving to Moving Phase.");
            }
        } else {
            System.out.println("Node is already occupied.");
        }
    }

    public void makeMove(int fromId, int toId) {
        Node from = board.getNode(fromId);
        Node to = board.getNode(toId);

        // Validate the move
        if ((board.isValidMove(from, to) || canFly(currentPlayer)) && from.getOccupant() == currentPlayer) {
            to.setOccupant(currentPlayer);  // Place the current player's piece
            from.setOccupant(null);         // Remove from previous location

            // Check for mill formation
            if (board.checkMill(to, currentPlayer)) {
                System.out.println(currentPlayer.getName() + " has formed a mill!");
                // Implement logic to remove an opponent's piece
            }

            // Switch players
            switchPlayer();
        } else {
            System.out.println("Invalid move.");
        }
    }

    public boolean canFly(Player player) {
        return player.getStonesOnBoard() == 3;
    }

    public void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getPhase() {
        return phase;
    }

    public boolean isPlacingPhase() {
        return phase == 1;
    }

    // Additional methods for handling game logic...
}