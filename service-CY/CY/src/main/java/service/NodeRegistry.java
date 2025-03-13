package service;

import model.Organization;
import node.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Temporary Node Registry */
public class NodeRegistry {

    private static volatile NodeRegistry instance;
    private Map<Organization, Set<Node>> allNodes;

    private NodeRegistry() {
        allNodes = new HashMap<>();
    }

    public static NodeRegistry getInstance() {
        if (instance == null) {
            synchronized (NodeRegistry.class) {
                if (instance == null) {
                    instance = new NodeRegistry();
                }
            }
        }
        return instance;
    }

    public void addNode(Organization organization, Node node) {
        allNodes.computeIfAbsent(organization, k -> new HashSet<>()).add(node);
    }

    public Node getNodeByID(int id) {
        for (Set<Node> nodes : allNodes.values()) {
            for (Node node : nodes) {
                if (node.getID() == id) {
                    return node;
                }
            }
        }
        return null;
    }
}
