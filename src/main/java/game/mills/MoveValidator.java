package game.mills;

public class MoveValidator {
    private Board board;

    public MoveValidator(Board board) {
        this.board = board;
    }

    public boolean isValidPlacement(Player player, int nodeID) {
        Node node = board.getNode(nodeID);
        return player.getStonesToPlace() > 0 && !node.isOccupied();
    }

    public boolean isValidMove(Player player, int nodeFrom, int nodeTo) {
        Node from = board.getNode(nodeFrom);
        Node to = board.getNode(nodeTo);
        return (board.isValidMove(from, to) || canFly(player)) && from.getOccupant() == player;
    }

    public boolean canFly(Player player) {
        return player.getStonesOnBoard() == 3;
    }
}
