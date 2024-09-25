package agents.neural_network;

import game.mills.NewGame;

public class SelfPlayTrainer {
    private NeuralNetworkAgent agent1;
    private NeuralNetworkAgent agent2;

    public SelfPlayTrainer() {
        this.agent1 = new NeuralNetworkAgent("model1.tf");
        this.agent2 = new NeuralNetworkAgent("model2.tf");
    }

    public void train(int epochs) {
        for (int i = 0; i < epochs; i++) {

        }
    }
}
