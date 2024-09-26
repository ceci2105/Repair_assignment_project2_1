package agents.neural_network;

import org.tensorflow.Graph;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Placeholder;
import org.tensorflow.types.TFloat16;

public class Model  {
    private static final long INPUT_SIZE = 0; // TODO: determine input size!
    private static final long HIDDEN_SIZE = 0; //TODO: Determine fitting hidden layer size
    private static final float OUTPUT_SIZE  = 24; // In the chosen architecture the model will output a probability distribution for all 24 Nodes of the board
    private SavedModelBundle saveModel;

    public Model() {
        createTrainSaveModel();
    }

    private void createTrainSaveModel() {
        try (Graph graph = new Graph()) {
            Ops tf  = Ops.create(graph);

        }
    }

}
