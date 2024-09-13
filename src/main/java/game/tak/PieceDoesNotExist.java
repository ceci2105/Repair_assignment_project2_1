package game.tak;

public class PieceDoesNotExist extends RuntimeException {
    public PieceDoesNotExist(String message) {
        super(message);
    }
}
