package game.tak;

public class StandingStone implements Piece {
    private final Player owner;

    public StandingStone(Player owner) {
        this.owner = owner;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public boolean isCapStone() {
        return false;
    }

    @Override
    public PieceType getType() {
        return PieceType.STANDING;
    }
}
