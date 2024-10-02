/**
package agents.neural_network;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.nn.core.Prelu;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.Trainer;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.Tracker;
import game.mills.Board;
import game.mills.Node;
import game.mills.Player;
import javafx.scene.paint.Color;
import lombok.Getter;

/**
 * Agent using Reinforcement Learning (RL)

public class RLAgent implements Player {
    @Getter
    private final String name;
    @Getter
    private final Color color;
    @Getter
    private int stonesToPlace;
    @Getter
    private int stonesOnBoard;
    @Getter
    private int uniqueID;
    private NDArray boardState;
    private SequentialBlock model;
    private Trainer trainer;

    public RLAgent(String name, Color color) {
        this.name = name;
        this.color = color;
        this.boardState = boardState;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
        NDManager manager = NDManager.newBaseManager();
        this.boardState = manager.zeros(new Shape(1, 24));

        // This is the model initialisation. We create a model with 128 input parameters and 24 outputs.
        this.model = new SequentialBlock();
        model.add(Linear.builder().setUnits(128).build());
        model.add(Activation.reluBlock());
        model.add(Linear.builder().setUnits(64).build());
        model.add(Activation.reluBlock());
        model.add(Linear.builder().setUnits(24).build());

        Loss loss = Loss.hingeLoss();
        Optimizer optimizer = Optimizer.adam().optLearningRateTracker(Tracker.fixed(0.001F)).build();
        DefaultTrainingConfig config = new DefaultTrainingConfig(loss)
                .optOptimizer(optimizer);
        //his.trainer = new Trainer(model, config)
    }

    /**
     * Updates the State of the board. Uses streaming for parallel computation
     * @param board

    public void updateBoardState(Board board) {
        board.getNodes().entrySet().stream().forEach(entry -> {
            int nodeID = entry.getKey();
            Node node = entry.getValue();
            if (node.getOccupant() == null) {
                boardState.set(new NDIndex(nodeID), 0);
            } else if (node.getOccupant().getUniqueID() == 1) {
                boardState.set(new NDIndex(nodeID), 1);
            } else if (node.getOccupant().getUniqueID() == 2) {
                boardState.set(new NDIndex(nodeID), 2);
            }
        });
    }


    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    public void decrementStonesOnBoard() {
        stonesOnBoard--;

    }
}
*/