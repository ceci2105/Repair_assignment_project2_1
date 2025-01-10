package Minimax;

public class DataGenerationRunner {
    public static void main(String[] args) {
        SelfPlayGenerator generator = new SelfPlayGenerator(1000, "training_data.txt");
        generator.generateGames();
    }
}
