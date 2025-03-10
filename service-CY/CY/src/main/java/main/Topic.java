package main;

import datatype.DataType;
import observerpattern.Publisher;
import observerpattern.Subscriber;

/** The component which the nodes will communicate over. In particular, output handles will publish to them, and
 * input handles will subscribe to them. */
public class Topic{

    private final String name;

    public Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
