package node;

import model.Message;
import model.Topic;
import algorithms.Algorithm;
import datatype.DataType;
import node.handle.InputHandle;
import utils.Pair;

import java.util.Collection;
import java.util.HashSet;

/** The only thing distinguishing a MiningNode from the more general OperatorNode is that its algorithm outputs a pair:
 * a DataType instance representing the output of the algorithm, and a boolean stating whether the node should publish
 * the output.*/
public class MiningNode<T extends DataType> extends OperatorNode<T>{
    private final Collection<InputHandle<?>> inputHandles;
    private final Algorithm<Pair<T, Boolean>> algorithm;

    public MiningNode(String name, String description, Collection<Topic> inputTopics, Algorithm<Pair<T, Boolean>> algorithm) {
        super(name, description);

        assert !inputTopics.isEmpty() : "OperatorNode inheritors must have at least 1 inputHandle";
        this.algorithm = algorithm;

        // create input handles
        inputHandles = new HashSet<>();
        for (Topic topic : inputTopics) {
            InputHandle<?> handle = makeInputHandle(topic);
            inputHandles.add(handle);
        }
    }

    @Override
    public void observe(model.Message<T> message) {
        Pair<T, Boolean> output = algorithm.runAlgorithm(message.data());
        if (output.getSecond()) {
            Message<T> outputMessage = new Message<>(output.getFirst());
            publish(outputMessage);
        }
    }
}
