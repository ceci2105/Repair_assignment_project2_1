package game.mills;

/**
 * The InvalidMove class represents an exception that is thrown when a player attempts 
 * to make an invalid move in the game.
 * It extends the RuntimeException class and provides a custom message indicating the reason for the invalid move.
 */
public class InvalidMove extends RuntimeException {

    /**
     * Constructs a new InvalidMove exception with the specified detail message.
     *
     * @param message the detail message explaining why the move is invalid.
     */
    public InvalidMove(String message) {
        super(message);
    }
}
