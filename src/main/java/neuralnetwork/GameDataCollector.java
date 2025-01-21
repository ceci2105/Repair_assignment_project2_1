package neuralnetwork;

import game.mills.Board;
import game.mills.Game;
import game.mills.Player;
import javafx.scene.paint.Color;
import minimax.MinimaxAIPlayer;
import lombok.extern.java.Log;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.List;

@Log
public class GameDataCollector {
    private List<BoardState> trainingData;
    private Board board;
    private Game game;
    private int depth = 3;

    public GameDataCollector() {
        this.trainingData = new ArrayList<>();
    }

    // Represents a single board state and its evaluation
    public static class BoardState {
        public float[] boardRepresentation;  // 3 channels x 24 positions
        public float outcome;                // 1.0 for win, 0.0 for loss, 0.5 for ongoing

        public BoardState(float[] board, float outcome) {
            this.boardRepresentation = board;
            this.outcome = outcome;
        }
    }

    // Convert board to neural network input format
    public float[] boardToNNInput(Board board) {
        float[] input = new float[72];  // 24 positions x 3 channels

        // Channel 1: Current player's pieces
        // Channel 2: Opponent's pieces
        // Channel 3: Empty positions
        for (int i = 0; i < 24; i++) {
            Player occupant = board.getNode(i).getOccupant();
            if (occupant == null) {
                input[i + 48] = 1.0f;      // Empty in channel 3
            } else if (occupant == game.getCurrentPlayer()) {
                input[i] = 1.0f;           // Current player in channel 1
            } else {
                input[i + 24] = 1.0f;      // Opponent in channel 2
            }
        }

        return input;
    }

    // Record a game state for training
    public void recordGameState(Board board, float outcome) {
        float[] boardInput = boardToNNInput(board);
        trainingData.add(new BoardState(boardInput, outcome));
    }

    // Generate training data from self-play games
    public void generateTrainingData(int numGames) {
        for (int i = 0; i < numGames; i++) {
            MinimaxAIPlayer minimaxAIPlayer = new MinimaxAIPlayer("White", Color.WHITE, depth, game);
            MinimaxAIPlayer minimaxAIPlayer2 = new MinimaxAIPlayer("Black", Color.BLACK, depth, game);
            Game game = new Game(minimaxAIPlayer, minimaxAIPlayer2);
            while (!game.isGameOver) {
                // Record the current board state
                float outcome = 0.5f;  // Game ongoing
                if (game.isGameOver) {
                    outcome = game.getWinner() == game.getCurrentPlayer() ? 1.0f : 0.0f;
                }
                recordGameState(game.getBoard(), outcome);

                // Make move using existing minimax
                // You'll need to adapt this based on your current implementation
            }
        }
    }

    // Convert collected data to DL4J format
    public INDArray getTrainingFeatures() {
        INDArray features = Nd4j.create(trainingData.size(), 3, 24);
        for (int i = 0; i < trainingData.size(); i++) {
            float[] boardData = trainingData.get(i).boardRepresentation;
            for (int j = 0; j < boardData.length; j++) {
                features.putScalar(new int[]{i, j / 24, j % 24}, boardData[j]);
            }
        }
        return features;
    }

    public INDArray getTrainingLabels() {
        INDArray labels = Nd4j.create(trainingData.size(), 1);
        for (int i = 0; i < trainingData.size(); i++) {
            labels.putScalar(i, trainingData.get(i).outcome);
        }
        return labels;
    }
}