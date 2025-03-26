package utils;

public class IDGenerator {
    private static int nextID = 0;

    public static int generate() { return nextID++; }
}
