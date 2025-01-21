package agents.neural_network;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import lombok.extern.java.Log;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

@Log
public class DataLoader {
    protected static List<INDArray> readData() {
        String dir = "Data";
        List<Map<INDArray, INDArray>> dataList = new ArrayList<>();

        try {
            Files.list(Paths.get(dir)).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath.toFile().toPath()))) {
                        Object obj = ois.readObject();
                        if (obj instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) obj;
                            if (!map.isEmpty() && map.keySet().iterator().next() instanceof INDArray && map.values().iterator().next() instanceof INDArray) {
                                dataList.add((Map<INDArray, INDArray>) map);
                            } else {
                                log.log(Level.WARNING, "Map contains unexpected types: " + map);
                            }
                        } else {
                            log.log(Level.WARNING, "Deserialized object is not a Map: " + obj.getClass().getName());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (dataList.isEmpty()) {
            log.log(Level.WARNING, "Data List is empty!");
            throw new IllegalStateException();
        }

        List<INDArray> returnedList = new ArrayList<>();
        List<INDArray> inputs = new ArrayList<>();
        List<INDArray> labels = new ArrayList<>();

        for (Map<INDArray, INDArray> data : dataList) {
            for (Map.Entry<INDArray, INDArray> entry : data.entrySet()) {
                labels.add(entry.getKey());
                inputs.add(entry.getValue());
            }
        }

        if (inputs.isEmpty()) {
            log.log(Level.WARNING, "Input is null!");
            throw new IllegalStateException();
        } else if (labels.isEmpty()) {
            log.log(Level.WARNING, "Labels is null!");
            throw new IllegalStateException();
        }

        INDArray inputArray = Nd4j.vstack(inputs);
        INDArray labelArray = Nd4j.vstack(labels);

        returnedList.add(inputArray);
        returnedList.add(labelArray);
        return returnedList;
    }
}