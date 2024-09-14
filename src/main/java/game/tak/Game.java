package game.tak;

/**
 * This class handles the main game loop and necessary functions for the games to be played.
 */
public class Game {
    public Board board;
    public Player player1;
    public Player player2;

    /**
     * Class constructor.
     */
    public Game() {
        int boardSize = 5;
        board = new Board(boardSize);

        player1 = new Player("Player 1");
        player2 = new Player("Player 2");

        if (Math.random() < 0.5) {
            player1.setCurrentPlayer(true);
        }

        //TODO: Other initialisations.
    }

    /**
     * Switches the current player.
     * @param currentPlayer The current player.
     * @param nextPlayer The next player to be set active.
     * @throws PlayerIsNotActive when the current player isn't the active player.
     */
    public void switchPlayer(Player currentPlayer, Player nextPlayer) {
        if (!currentPlayer.isCurrentPlayer()) {
            throw new PlayerIsNotActive(currentPlayer.getName() + " is not the active player!");
        } else {
            currentPlayer.setCurrentPlayer(false);
            nextPlayer.setCurrentPlayer(true);
        }
    }
}
