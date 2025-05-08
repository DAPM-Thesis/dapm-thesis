package utils.graph;

import java.util.*;

public class DG<T, U> {
    private final Map<Edge<T>, U> adjacencyList = new HashMap<>();

    public Set<T> getNeighbors(T node) {
        Set<T> neighbors = new HashSet<>();
        for (Edge<T> edge : adjacencyList.keySet()) {
            if (edge.getFrom().equals(node)) {
                neighbors.add(edge.getTo());
            }
        }
        return neighbors;
    }

    public Set<T> getNodes() {
        Set<T> nodes = new HashSet<>();
        for (Edge<T> edge : adjacencyList.keySet()) {
            nodes.add(edge.getFrom());
            nodes.add(edge.getTo());
        }
        return nodes;
    }

    public U getEdgeAttribute(T from, T to) {
        return adjacencyList.get(new Edge<>(from, to));
    }

    public void addEdgeWithAttribute(T from, T to, U attr) {
        adjacencyList.put(new Edge<>(from, to), attr);
    }

}

