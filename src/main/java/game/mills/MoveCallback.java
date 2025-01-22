package game.mills;

/**
 * A functional interface that defines a callback for monitoring game moves.
 * This interface allows external components to observe and react to changes
 * in the game state without tightly coupling them to the game logic.
 */
@FunctionalInterface
public interface MoveCallback {
    /**
     * Called after each move in the game to notify observers of the new game state.
     * 
     * @param board         The current state of the game board
     * @param currentPlayer The player who just completed their move
     */
    void onMove(Board board, Player currentPlayer);
}
