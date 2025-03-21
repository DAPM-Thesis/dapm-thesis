package impl;

import algorithm.Algorithm;

public class MyAlgorithm implements Algorithm<String, String> {

    @Override
    public String run(String s) {
        // Just passing on the String
        return s;
    }
}
