package node;


import node.OperatorNode;
import model.Topic;
import model.Message;
import algorithms.Algorithm;
import datatype.DataType;
import node.handle.InputHandle;
import node.handle.OutputHandle;

import java.util.Collection;

public class GenericNode<T extends DataType> extends OperatorNode<T> {
    private final InputHandle<?> inputHandle;
    private final Algorithm<T> algorithm;

    public GenericNode(String name, String description, Topic inputTopic, Algorithm<T> algorithm) {
        super(name, description);
        inputHandle = InputHandle.createForTopic(inputTopic, this);
        this.algorithm = algorithm;
    }

    @Override
    public void observe(Message<T> message) {
        publish(new Message<>(algorithm.runAlgorithm(message.data())));
    }
}
