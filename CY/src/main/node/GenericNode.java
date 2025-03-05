package main.node;

import main.Message;
import main.Topic;
import main.algorithms.Algorithm;
import main.datatype.DataType;
import main.node.handle.InputHandle;
import main.node.handle.OutputHandle;

import java.util.Collection;

public class GenericNode<T extends DataType> extends OperatorNode<T> {
    private final InputHandle<?> inputHandle;
    private final Algorithm<T> algorithm;

    public GenericNode(String name, String description, Topic<?> inputTopic, Algorithm<T> algorithm) {
        super(name, description);
        inputHandle = InputHandle.createForTopic(inputTopic);
        this.algorithm = algorithm;
    }

    @Override
    public void observe(Message<T> message) {
        publish(new Message<>(algorithm.runAlgorithm(message.data())));
    }
}
