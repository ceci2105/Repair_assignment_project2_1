package agents.neural_network;

import ai.djl.ndarray.NDArray;
import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
import lombok.extern.java.Log;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;


import java.util.Map;
import java.util.logging.Level;

@Log
public class GNN {
    private ComputationGraph gnn;

    public GNN() {
        ComputationGraphConfiguration configuration = new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .graphBuilder()
                .addInputs("input")
                .setInputTypes(InputType.feedForward(24))
                .addLayer("dense1", new DenseLayer.Builder().nIn(24).nOut(64)
                        .activation(Activation.RELU)
                        .build(), "input")
                .addLayer("dense2", new DenseLayer.Builder().nIn(64).nOut(32)
                        .activation(Activation.RELU)
                        .build(), "dense1")
                .addLayer("output", new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(32).nOut(1)
                        .build(), "dense2")
                .setOutputs("output")
                .build();
        this.gnn = new ComputationGraph(configuration);
        gnn.init();
        log.log(Level.INFO, gnn.summary());
    }


    public static INDArray boardToINDArray(Board board, Game game) {
        int numNodes = 24;
        INDArray boardArray = Nd4j.zeros(1,numNodes);

        Map<Integer, Node> nodes = board.getNodes();
        for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
            int nodeId = entry.getKey();
            Node node = entry.getValue();
            Player occupant = node.getOccupant();
            if (occupant != null) {
                if (occupant.equals(game.getPlayer1())) {
                    boardArray.putScalar(nodeId, 1);
                } else if (occupant.equals(game.getPlayer2())) {
                    boardArray.putScalar(nodeId, 2);
                } else {
                    boardArray.putScalar(nodeId, 0);
                }
            }
        }
        return boardArray;
    }

    public void fit(INDArray[] inputs) {
        DataSet dataSet = new DataSet(inputs[0], inputs[1]);
        gnn.fit(dataSet);
    }
}