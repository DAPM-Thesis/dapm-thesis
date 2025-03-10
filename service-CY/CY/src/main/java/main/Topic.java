package main;

import main.datatype.DataType;
import main.observerpattern.Publisher;
import main.observerpattern.Subscriber;

import java.util.Collection;
import java.util.HashSet;

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
