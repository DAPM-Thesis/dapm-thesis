package main.node;

import main.Message;
import main.Topic;
import main.algorithms.Algorithm;
import main.datatype.DataType;
import main.utils.Pair;

import java.util.Collection;

/** The only thing distinguishing a MiningNode from the more general OperatorNode is that its algorithm outputs a pair:
 * a DataType instance representing the output of the algorithm, and a boolean stating whether the node should publish
 * the output.*/
public class MiningNode<T extends DataType> extends OperatorNode<T>{
    Algorithm<Pair<T, Boolean>> algorithm;

    public MiningNode(String name, String description, Collection<Topic<?>> inputTopics, Algorithm<Pair<T, Boolean>> algorithm) {
        super(name, description, inputTopics);
        this.algorithm = algorithm;
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
