package game.tak;

import java.util.List;

public class Player {
    private List<Piece> capturedPieces;
    private boolean currentPlayer;
    private String name;

    public Player(String name) {
        this.name = name;
    }
    public List<Piece> getCapturedPieces() {
        return capturedPieces;
    }

    public void setCapturedPieces(List<Piece> capturedPieces) {
        this.capturedPieces = capturedPieces;
    }

    public void addCapturedPiece(Piece capturedPiece) {
        capturedPieces.add(capturedPiece);
    }

    public void setCurrentPlayer(boolean currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public boolean isCurrentPlayer() {
        return currentPlayer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
