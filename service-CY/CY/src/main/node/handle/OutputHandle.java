package main.node.handle;

import main.datatype.DataType;
import main.Message;
import main.Topic;
import main.service.Producer;

/** The component of Node which is responsible for publishing node output to topics. */
public class OutputHandle<T extends DataType> extends Handle<T> {

    private Producer producer;

    public OutputHandle(Topic topic) {
        super(topic);
        producer = new Producer();
    }

    public void publish(Message<T> msg) {
        this.producer.publish(getTopic().getName(), msg);
    }

}
