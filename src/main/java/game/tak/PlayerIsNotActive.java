package game.tak;

public class PlayerIsNotActive extends RuntimeException {
    public PlayerIsNotActive(String message) {
        super(message);
    }
}
