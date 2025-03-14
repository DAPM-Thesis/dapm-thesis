package model;

import node.GenericNode;
import node.MiningNode;
import node.Node;
import node.OperatorNode;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class Pipeline {

    private final int ID;
    private final Map<Node, Node> connections;
    private final Collection<Topic> topics;
    private final Collection<Node> allNodes;

    public Pipeline(int ID) {
        this.ID = ID;
        connections = new HashMap<Node, Node>();
        topics = new HashSet<>();
        allNodes = new HashSet<>();
    }

    public void addNodes(Node... nodes) {
        allNodes.addAll(Arrays.asList(nodes));
    }

    public void addConnection(Node from, Node to) {
        connections.put(from, to);
    }

    public void addTopic(Topic topic) {
        topics.add(topic);
    }

    public int getID() {
        return ID;
    }

    public Node getNodeByID(int id) {
        for(Node node : allNodes) {
            if(node.getID() == id) {
                return node;
            }
        }
        return null;
    }
}
