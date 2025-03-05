package main.node.handle;

import main.datatype.DataType;
import main.Message;
import main.Topic;

/** The component of Node which is responsible for publishing node output to topics. */
public class OutputHandle<T extends DataType> extends Handle<T> {

    public OutputHandle(Topic<T> topic) {
        super(topic);
    }

    public void publish(Message<T> msg) {this.topic.publish(msg); }

}
