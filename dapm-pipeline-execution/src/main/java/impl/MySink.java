package impl;

import pipeline.processingelement.Sink;

public class MySink extends Sink<String> {

    @Override
    public void observe(String s) {
        System.out.println("Output: " + s);
    }
}
