package node;

import model.Message;
import datatype.DataType;
import model.Topic;
import node.handle.OutputHandle;
import observerpattern.Subscriber;


public abstract class OperatorNode<T extends DataType> extends Node implements Subscriber<Message<T>> {

    protected final OutputHandle<T> outputHandle;

    public OperatorNode(String name, String description) {
        super(name, description);
        outputHandle = new OutputHandle<>();
    }

    public void setOutputTopic(Topic topic) {
        outputHandle.setTopic(topic);
    }

    public Topic getOutputTopic() { return outputHandle.getTopic(); }

    public void publish(Message<T> message) {
        this.outputHandle.publish(message);
    }

}
