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

    public MiningNode(String name, String description, Algorithm<Pair<T, Boolean>> algorithm) {
        super(name, description);
        this.algorithm = algorithm;
        inputHandles = new HashSet<>();
    }

    public void setInputTopic(Topic topic) {
        InputHandle<T> inputHandle = new InputHandle<>();
        inputHandle.setTopic(topic);
        inputHandles.add(inputHandle);
    }

    public Collection<Topic> getInputTopics() {
        Collection<Topic> inputTopics = new HashSet<>();
        for (InputHandle<?> inputHandle : inputHandles) {
            inputTopics.add(inputHandle.getTopic());
        }
        return inputTopics;
    }

    @Override
    public void observe(Message<T> message) {
        Pair<T, Boolean> output = algorithm.runAlgorithm(message.data());
        if (output.getSecond()) {
            Message<T> outputMessage = new Message<>(output.getFirst());
            publish(outputMessage);
        }
    }
}
