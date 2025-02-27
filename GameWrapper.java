package MCTS;

import game.mills.Board;
import game.mills.Game;
import game.mills.Player;

public class GameWrapper {
    private Board board;
    private Game game;
    private Player currentPlayer;
    private Player opponent;

    public GameWrapper(Board board, Player currentPlayer, Player opponent) {
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.opponent = opponent;
    }

    public Player[] getBoardPositions() {
        Player[] positions = new Player[24];
        for (int i = 0; i < 24; i++) {
            positions[i] = board.getNode(i).getOccupant();
        }
        return positions;
    }

    public List<Integer> getValidMoves() {
        List<Integer> validMoves = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (!board.getNode(i).isOccupied()) {
                validMoves.add(i);
            }
        }
        return validMoves;
    }

    public boolean isTerminalState() {
        return game.isGameOver();
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void makeMove(int move) {
        board.placePiece(currentPlayer, move);
    }

    public boolean checkMill(int move, Player player) {
        Node node = board.getNode(move);
        return board.checkMill(node, player);
    }

    public void removeOpponentStone(int move) {
        Node node = board.getNode(move);
        Player opponent = node.getOccupant();
        board.removePiece(opponent, move);
    }
}
