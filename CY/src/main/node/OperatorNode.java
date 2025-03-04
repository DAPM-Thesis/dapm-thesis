package main.node;

import main.algorithms.Algorithm;
import main.utils.IDGenerator;
import main.datatype.DataType;
import main.Topic;
import main.node.handle.InputHandle;
import main.node.handle.OutputHandle;

import java.util.Collection;
import java.util.HashSet;

public class OperatorNode extends Node {
    private final Collection<InputHandle<?>> inputHandles;
    private final OutputHandle<?> outputHandle;
    main.algorithms.Algorithm<?> Algorithm;

    public <T extends DataType> OperatorNode(String name, String description, Collection<Topic<?>> inputTopics, Algorithm<T> algorithm) {
        super(name, IDGenerator.newID(), description);

        inputHandles = new HashSet<>();
        for (Topic<?> topic : inputTopics) {
            addInputHandle(topic);
        }

        Topic<T> outputTopic = new Topic<>();
        outputHandle = new OutputHandle<>(outputTopic);
    }

    private <T extends DataType> void addInputHandle(Topic<T> topic) {
        InputHandle<T> handle = InputHandle.createForTopic(topic);
        inputHandles.add(handle);
    }

    public Topic<?> getOutputTopic() { return outputHandle.getTopic(); }
}
