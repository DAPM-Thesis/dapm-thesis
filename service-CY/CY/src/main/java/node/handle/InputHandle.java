package node.handle;

import model.Message;
import datatype.DataType;
import observerpattern.Publisher;
import observerpattern.Subscriber;
import service.Consumer;

/** Input handle is responsible for receiving messages from a topic and publishing them to its node */
public class InputHandle<T extends DataType> extends Handle<T> implements Subscriber<Message<T>>, Publisher<Message<T>> {
    private Subscriber<Message<T>> node;
    private Consumer consumer;

    // private constructor to indicate that inputhandles should only be created by Nodes (via createForTopic)
    private InputHandle(model.Topic topic) {
        super(topic);
        consumer = new Consumer();
        consumer.subscribe(topic.getName(), this);
    }

    @Override
    public void observe(Message<T> message) {
        // Pass on the message received from the model.Topic to the input handle's Node.
        System.out.println("Received in input handle: " +message);
        publish(message);
    }

    public static <T extends DataType> InputHandle<T> createForTopic(model.Topic topic, Subscriber<Message<T>> node) {
        InputHandle<T> inputHandle = new InputHandle<>(topic);
        inputHandle.subscribe(node);
        return inputHandle;
    }

    @Override
    public void subscribe(Subscriber<Message<T>> subscriber) {
        this.node = subscriber;
    }

    @Override
    public void unsubscribe(Subscriber<Message<T>> subscriber) {
        throw new IllegalCallerException("An input handle's lifetime is the same as the node it is created for. It should never unsubscribe from its node.");
    }

    @Override
    public void publish(Message<T> message) {
        assert node != null : "An input handle must always be attached to a node.";
        node.observe(message);
    }
}
