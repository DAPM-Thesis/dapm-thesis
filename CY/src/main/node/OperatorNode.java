package main.node;

import main.Message;
import main.algorithms.Algorithm;
import main.datatype.DataType;
import main.Topic;
import main.node.handle.InputHandle;
import main.node.handle.OutputHandle;
import main.observerpattern.Subscriber;
import main.utils.Pair;

import java.util.Collection;
import java.util.HashSet;

public class OperatorNode extends Node implements Subscriber<Message<? extends DataType>> {
    private final Collection<InputHandle<?>> inputHandles;
    private final OutputHandle<?> outputHandle;
    private Algorithm<?> algorithm;

    public <T extends DataType> OperatorNode(String name, String description, Collection<Topic<?>> inputTopics, Algorithm<T> algorithm) {
        super(name, description);

        // create input handles
        inputHandles = new HashSet<>();
        for (Topic<?> topic : inputTopics) {
            addInputHandle(topic);
        }

        // create output handle
        Topic<T> outputTopic = new Topic<>();
        outputHandle = new OutputHandle<>(outputTopic);
    }

    /** Creates a (new) input handle for the node.
     * @param topic the topic that the input handle subscribes to receive streamed items.*/
    private <T extends DataType> void addInputHandle(Topic<T> topic) {
        InputHandle<T> handle = InputHandle.createForTopic(topic);
        inputHandles.add(handle);
    }

    public Topic<?> getOutputTopic() { return outputHandle.getTopic(); }

    @Override
    public void observe(Message<? extends DataType> message) {
        DataType data = message.data();
        Pair<?, Boolean> output = algorithm.runAlgorithm(data);
        if (output.getSecond()) {
            Message<?> outputMessage = new Message<DataType>((DataType) output.getFirst());
            outputHandle.publish(outputMessage);
        }
    }
}
