package game.mills;

/**
 * The MoveValidator class is responsible for validating player moves and placements
 * on the game board. It checks whether a move or placement is valid 
 * based on the current game state and rules.
 */
public class MoveValidator {
    private Board board;

    /**
     * Constructs a MoveValidator object associated with a game board.
     *
     * @param board the board that the validator will use to check moves and placements.
     */
    public MoveValidator(Board board) {
        this.board = board;
    }

    /**
     * Validates whether the given player can place a stone at the specified node.
     * The placement is valid if the player has stones left to place and the node is not occupied.
     *
     * @param player the player attempting to place a stone.
     * @param nodeID the ID of the node where the player wants to place the stone.
     * @return true if the placement is valid, false otherwise.
     */
    public boolean isValidPlacement(Player player, int nodeID) {
        Node node = board.getNode(nodeID);

        if (node.isOccupied()) {
            System.out.println("[DEBUG] Node " + nodeID + " is already occupied!");
            return false;
        }
        
        if (player.getStonesToPlace() <= 0) {
            System.out.println("[DEBUG] Player " + player.getName() + " has no stones left to place!");
            return false;
        }
        return player.getStonesToPlace() > 0 && !node.isOccupied();
    }

    /**
     * Validates whether the given player can move a stone from one node to another.
     * A move is valid if the destination node is adjacent to the source node, or if the player is allowed to "fly"
     * (i.e., the player has exactly 3 stones on the board).
     *
     * @param player  the player attempting to make the move.
     * @param nodeFrom the ID of the node where the stone is currently located.
     * @param nodeTo   the ID of the node where the player wants to move the stone.
     * @return true if the move is valid, false otherwise.
     */
    public boolean isValidMove(Player player, int nodeFrom, int nodeTo) {
        Node from = board.getNode(nodeFrom);
        Node to = board.getNode(nodeTo);
        // Allow flying if humanPlayer has 3 stones, otherwise check if move is valid based on adjacency
        return (canFly(player) || board.isValidMove(from, to)) && from.getOccupant() == player;
    }

    /**
     * Checks if the given player can "fly," which allows the player to move a stone to any open node on the board.
     * A player is allowed to fly if they have exactly 3 stones left on the board.
     *
     * @param player the player whose ability to fly is being checked.
     * @return true if the player can fly, false otherwise.
     */
    public boolean canFly(Player player) {
        return player.getStonesOnBoard() == 3;
    }
}
