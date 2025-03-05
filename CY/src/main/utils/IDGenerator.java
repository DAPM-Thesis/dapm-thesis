package main.utils;

/** A simple, synchronized (non thread-safe) ID Generation static class. */
public class IDGenerator {
    private static int ID = 1;

    private IDGenerator() {
        throw new UnsupportedOperationException("Static class; should not be instantiated.");
    }

    public static int newID() {
        return ID++;
    }
}
