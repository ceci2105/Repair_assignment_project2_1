package Minimax;

import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameRecorder {
    private static class GameState {
        String boardState;
        int playerStonesToPlace;
        int opponentStonesToPlace;
        int playerStonesOnBoard;
        int opponentStonesOnBoard;
        boolean playerWon;

        public GameState(Board board, Player player, Player opponent, boolean playerWon) {
            this.boardState = convertBoardToString(board, player, opponent);
            this.playerStonesToPlace = player.getStonesToPlace();
            this.opponentStonesToPlace = opponent.getStonesToPlace();
            this.playerStonesOnBoard = countStonesOnBoard(board, player);
            this.opponentStonesOnBoard = countStonesOnBoard(board, opponent);
            this.playerWon = playerWon;
        }

        private String convertBoardToString(Board board, Player player, Player opponent) {
            StringBuilder state = new StringBuilder();
            for (int i = 0; i < 24; i++) {
                Node node = board.getNode(i);
                if (!node.isOccupied()) {
                    state.append('O');
                } else if (node.getOccupant() == player) {
                    state.append('M');
                } else if (node.getOccupant() == opponent) {
                    state.append('E');
                }
            }
            return state.toString();
        }

        private int countStonesOnBoard(Board board, Player player) {
            int count = 0;
            for (Node node : board.getNodes().values()) {
                if (node.isOccupied() && node.getOccupant() == player) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public String toString() {
            return String.format("%s%d%d%d%d-%b",
                    boardState,
                    playerStonesToPlace,
                    opponentStonesToPlace,
                    playerStonesOnBoard,
                    opponentStonesOnBoard,
                    playerWon);
        }
    }

    private List<GameState> gameStates;
    private final String outputFile;

    public GameRecorder(String outputFile) {
        this.gameStates = new ArrayList<>();
        this.outputFile = outputFile;
    }

    public void recordState(Board board, Player player, Player opponent, boolean playerWon) {
        gameStates.add(new GameState(board, player, opponent, playerWon));
    }

    public void saveToFile() {
        try (FileWriter writer = new FileWriter(outputFile, true)) {
            for (GameState state : gameStates) {
                writer.write(state.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Clear the states after saving
        gameStates.clear();
    }
}