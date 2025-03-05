package main.node.handle;

import main.Message;
import main.datatype.DataType;
import main.Topic;
import main.observerpattern.Subscriber;

public class InputHandle<T extends DataType> extends Handle<T> implements Subscriber<Message> {

    // private constructor to indicate that inputhandles should only be created by Nodes (via createForTopic)
    private InputHandle(Topic<T> topic) {
        super(topic);
    }

    @Override
    public void observe(Message message) {
        // TODO: implement communication from input handle to Node.
    }

    public static <T extends DataType> InputHandle<T> createForTopic(Topic<T> topic) {
        InputHandle<T> handle = new InputHandle<>(topic);
        topic.subscribe(handle);
        return handle;
    }
}
