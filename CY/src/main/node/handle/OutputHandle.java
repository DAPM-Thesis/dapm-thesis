package main.node.handle;

import main.datatype.DataType;
import main.Message;
import main.Topic;
import main.observerpattern.Publisher;

public class OutputHandle<T extends DataType> extends Handle<T> {

    public OutputHandle(Topic<T> topic) {
        super(topic);
    }

}
