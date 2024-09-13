package game.tak;

public class CapStone implements Piece {
    public final Player owner;

    public CapStone(Player owner) {
        this.owner = owner;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public boolean isCapStone() {
        return true;
    }

    @Override
    public PieceType getType() {
        return PieceType.CAPSTONE;
    }
}
