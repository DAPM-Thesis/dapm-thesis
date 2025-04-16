package utils;

import java.util.*;

public class DAG<T> {
    private final Map<T, Set<T>> adjacencyList = new HashMap<>();

    public void addNode(T node) {
        adjacencyList.putIfAbsent(node, new HashSet<>());
    }

    public void addEdge(T from, T to) {
        addNode(from);
        addNode(to);
        if (createsCycle(from, to)) {
            throw new IllegalArgumentException("Adding this edge would create a cycle: " + from + " -> " + to);
        }
        adjacencyList.get(from).add(to);
    }

    public Set<T> getNeighbors(T node) {
        return adjacencyList.getOrDefault(node, Collections.emptySet());
    }

    public Set<T> getNodes() {
        return adjacencyList.keySet();
    }

    private boolean createsCycle(T from, T to) {
        Set<T> visited = new HashSet<>();
        Deque<T> stack = new ArrayDeque<>();
        stack.push(to);

        while (!stack.isEmpty()) {
            T current = stack.pop();
            if (current.equals(from)) {
                return true;
            }
            if (visited.add(current)) {
                stack.addAll(getNeighbors(current));
            }
        }
        return false;
    }

    public List<T> bfs(T start) {
        List<T> result = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        Queue<T> queue = new LinkedList<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            T current = queue.poll();
            result.add(current);

            for (T neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return result;
    }
}
