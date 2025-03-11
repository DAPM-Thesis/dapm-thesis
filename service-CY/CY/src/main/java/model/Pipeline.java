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

    private final Map<Node, Node> connections;
    private final Collection<Topic> topics;
    private final Collection<Node> nodes;

    public Pipeline() {
        connections = new HashMap<Node, Node>();
        topics = new HashSet<>();
        nodes = new HashSet<>();
    }

    public void addNode(Node node) {
        if(!nodes.contains(node)) {
            nodes.add(node);
        }
    }

    public void addConnection(Node from, Node to) {
        connections.put(from, to);
    }

    public void addTopic(Topic topic) {
        topics.add(topic);
    }

}
