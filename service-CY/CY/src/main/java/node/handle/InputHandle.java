package node.handle;

import datatype.serialization.deserialization.DataTypeFactory;
import main.Message;
import datatype.DataType;
import main.Topic;
import observerpattern.Publisher;
import observerpattern.Subscriber;
import service.Consumer;

/** Input handle is responsible for receiving messages from a topic and publishing them to its node */
public class InputHandle<T extends DataType> extends Handle<T> implements Subscriber<Message<String>>, Publisher<Message<T>> {
    private Subscriber<Message<T>> node;
    private Consumer consumer;

    // private constructor to indicate that inputhandles should only be created by Nodes (via createForTopic)
    private InputHandle(Topic topic) {
        super(topic);
        consumer = new Consumer();
        consumer.subscribe(topic.getName(), this);
    }

    @Override
    public void observe(Message<String> message) {
        // Pass on the message received from the Topic to the input handle's Node.
        T data = (T) DataTypeFactory.deserialize(message.data());
        Message<T> dataTypeMessage = new Message<>(data);
        System.out.println("Received in input handle: " + dataTypeMessage);
        publish(dataTypeMessage);
    }

    public static <T extends DataType> InputHandle<T> createForTopic(Topic topic, Subscriber<Message<T>> node) {
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
