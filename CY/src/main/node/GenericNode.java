package main.node;

import main.Message;
import main.Topic;
import main.algorithms.Algorithm;
import main.datatype.DataType;

import java.util.Collection;

public class GenericNode<T extends DataType> extends OperatorNode<T> {
    Algorithm<T> algorithm;

    public GenericNode(String name, String description, Collection<Topic<?>> inputTopics, Algorithm<T> algorithm) {
        super(name, description, inputTopics);
        this.algorithm = algorithm;
    }

    @Override
    public void observe(Message<T> message) {
        publish(new Message<>(algorithm.runAlgorithm(message.data())));
    }
}
