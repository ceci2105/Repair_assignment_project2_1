package Minimax;

import com.sun.prism.paint.Color;

import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;

public class SelfPlayGenerator {
    // private final MinimaxAlgorithm minimaxBot;
    private final GameRecorder recorder;
    private final int numGames;

    public SelfPlayGenerator(int numGames, String outputFile) {
        this.numGames = numGames;
        this.recorder = new GameRecorder(outputFile);
    }

    public void generateGames() {
        for (int i = 0; i < numGames; i++) {
            playGame();
        }
    }

    private void playGame() {
        Game game = new Game(null, null);
        Board board = new Board();
        int depth = 4;
        MinimaxAIPlayer player1 = new MinimaxAIPlayer("player1", Color.WHITE, game, depth);
        MinimaxAIPlayer player2 = new MinimaxAIPlayer("player2", Color.BLACK, game, depth);

        MinimaxAlgorithm bot1 = new MinimaxAlgorithm(game, 4);

        MinimaxAlgorithm bot2 = new MinimaxAlgorithm(game, 4);

        // Play until game over
        while (!game.isGameOver) {
            // Phase 1: Placement
            while (player1.getStonesToPlace() > 0 || player2.getStonesToPlace() > 0) {
                // Player 1's turn
                if (player1.getStonesToPlace() > 0) {
                    int placement = bot1.findBestPlacement(board, player1);
                    board.placePiece(player1, placement);
                    recorder.recordState(board, player1, player2, false);
                }

                // Player 2's turn
                if (player2.getStonesToPlace() > 0) {
                    int placement = bot2.findBestPlacement(board, player2);
                    board.placePiece(player2, placement);
                    recorder.recordState(board, player2, player1, false);
                }
            }

            // Phase 2: Movement
            while (!game.isGameOver) {
                // Player 1's turn
                Node[] move1 = bot1.findBestMove(board, player1, 2);
                if (move1[0] != null && move1[1] != null) {
                    board.movePiece(player1, move1[0].getId(), move1[1].getId());
                    recorder.recordState(board, player1, player2, false);
                }

                // Player 2's turn
                Node[] move2 = bot2.findBestMove(board, player2, 2);
                if (move2[0] != null && move2[1] != null) {
                    board.movePiece(player2, move2[0].getId(), move2[1].getId());
                    recorder.recordState(board, player2, player1, false);
                }
            }
        }

        // Record final states with game outcome
        boolean player1Won = checkWinner(board, player1);
        recorder.recordState(board, player1, player2, player1Won);
        recorder.recordState(board, player2, player1, !player1Won);

        // Save after each game
        recorder.saveToFile();
    }

    /*
     * private boolean checkWinner(Board board, Player player) {
     * // return true if opponent has less than 3 pieces or no valid moves
     * if (player.getStonesOnBoard() < 3) {
     * 
     * }
     * return false;
     * }
     */
}