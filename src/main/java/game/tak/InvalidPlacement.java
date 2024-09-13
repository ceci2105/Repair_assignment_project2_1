package game.tak;

public class InvalidPlacement extends RuntimeException {
    public InvalidPlacement(String message) {
        super(message);
    }
}
