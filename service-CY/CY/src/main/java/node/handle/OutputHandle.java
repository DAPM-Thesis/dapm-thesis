package node.handle;

import datatype.DataType;
import model.Topic;
import service.Producer;

/** The component of Node which is responsible for publishing node output to topics. */
public class OutputHandle<T extends DataType> extends Handle<T> {

    private Producer producer;

    public OutputHandle(Topic topic) {
        super(topic);
        producer = new Producer();
    }

    public void publish(model.Message<T> msg) {
        this.producer.publish(getTopic().getName(), msg);
    }

}
