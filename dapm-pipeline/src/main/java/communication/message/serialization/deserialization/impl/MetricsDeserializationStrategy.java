package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.Metrics;
import communication.message.serialization.deserialization.DeserializationStrategy;

import java.util.ArrayList;
import java.util.List;

public class MetricsDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String payload) {
        String listContents = payload.substring(1, payload.length() - 1); // "1, 2, 3, 4"
        String[] numbers = listContents.split(",");

        List<Integer> deserialization = new ArrayList<>();
        for (String num : numbers) {
            num = num.trim();
            if (num.isEmpty()) throw new IllegalArgumentException("Empty metric value in metrics deserialization.");
            deserialization.add(Integer.parseInt(num));
        }

        return new Metrics(deserialization);
    }
}
