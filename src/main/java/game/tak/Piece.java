package game.tak;

/**
 * Interface to represent the different types of pieces.
 */
public interface Piece {
    /**
     * Gets the owner of the given piece.
     * @return The owner of the piece.
     */
    Player getOwner();

    /**
     * Check if the piece is a Capstone.
     * @return True if it is, False otherwise.
     */
    boolean isCapStone();

    /**
     * Returns the type of piece annotated @Enum PieceType
     * @return The type of the piece.
     */
    PieceType getType();
}

