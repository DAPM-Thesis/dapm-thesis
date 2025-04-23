package utils;

import java.util.*;

public class DG<T> {
    private final Map<T, Set<T>> adjacencyList = new HashMap<>();

    public void addNode(T node) {
        adjacencyList.putIfAbsent(node, new HashSet<>());
    }

    public void addEdge(T from, T to) {
        addNode(from);
        addNode(to);
        adjacencyList.get(from).add(to);
    }

    public Set<T> getNeighbors(T node) {
        return adjacencyList.getOrDefault(node, Collections.emptySet());
    }

    public Set<T> getNodes() {
        return adjacencyList.keySet();
    }
}
