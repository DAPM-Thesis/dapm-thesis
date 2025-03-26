package impl;

import algorithm.Algorithm;

public class MyStringAlgorithm implements Algorithm<String, String> {

    @Override
    public String run(String s) {
        System.out.println("Applied: " + this);
        return s;
    }
}
