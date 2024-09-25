package agents.neural_network;

import game.mills.Board;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

public class NeuralNetworkAgent {
    private SavedModelBundle model;

    public NeuralNetworkAgent(String modelPath) {
        this.model = SavedModelBundle.load(modelPath, "serve");
    }

    public int[] selectMOve(Board board) {
        Tensor inputTensor = null;

        try (Session session = model.session()) {
            Tensor outputTensor = session.runner()
                    .feed("serving_default_input_1", inputTensor)
                    .fetch("StatefulPartitionedCall")
                    .run().get(0);
            int [] move = null;
            return move;
        }
    }
}
