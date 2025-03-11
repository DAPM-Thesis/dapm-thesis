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
    private final Algorithm<T> algorithm;
    private final InputHandle<?> inputHandle;

    public GenericNode(String name, String description, Algorithm<T> algorithm) {
        super(name, description);
        this.algorithm = algorithm;
        inputHandle = new InputHandle<>();
    }

    public void setInputTopic(Topic topic) {
        inputHandle.setTopic(topic);
    }

    public Topic getInputTopic() {
        return inputHandle.getTopic();
    }

    @Override
    public void observe(Message<T> message) {
        publish(new Message<>(algorithm.runAlgorithm(message.data())));
    }
}
