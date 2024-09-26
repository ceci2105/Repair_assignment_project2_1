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
        // Allow flying if humanPlayer has 3 stones, otherwise check if move is valid based on adjacency
        return (canFly(player) || board.isValidMove(from, to)) && from.getOccupant() == player;
    }

    public boolean canFly(Player player) {
        return player.getStonesOnBoard() == 3;
    }
}
