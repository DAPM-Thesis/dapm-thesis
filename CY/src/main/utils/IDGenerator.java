package main.utils;

public class IDGenerator {
    private static int ID = 1;

    private IDGenerator() {
        throw new UnsupportedOperationException("Static class; should not be instantiated.");
    }

    public static int newID() {
        return ID++;
    }
}
