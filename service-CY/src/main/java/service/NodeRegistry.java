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
    private Map<Integer, Set<Node>> allNodes;

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

    public void addNode(int organizationID, Node node) {
        allNodes.computeIfAbsent(organizationID, k -> new HashSet<>()).add(node);
    }

    public Node getNodeByID(int organizationID, int nodeID) {
        Set<Node> nodes = allNodes.get(organizationID);
        if (nodes == null) {
            return null;
        }
        for (Node node : nodes) {
            if (node.getID() == nodeID) {
                return node;
            }
        }
        return null;
    }
}
