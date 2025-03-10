package node;

import algorithms.Algorithm;
import datatype.DataType;
import node.handle.InputHandle;

public class GenericNode<T extends DataType> extends OperatorNode<T> {
    private final InputHandle<?> inputHandle;
    private final Algorithm<T> algorithm;

    public GenericNode(String name, String description, model.Topic inputTopic, Algorithm<T> algorithm) {
        super(name, description);
        inputHandle = InputHandle.createForTopic(inputTopic, this);
        this.algorithm = algorithm;
    }

    @Override
    public void observe(model.Message<T> message) {
        publish(new model.Message<>(algorithm.runAlgorithm(message.data())));
    }
}
