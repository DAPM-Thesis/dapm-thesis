package service;

import model.Pipeline;
import model.Topic;
import node.GenericNode;
import node.MiningNode;
import node.Node;
import node.OperatorNode;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class PipeLineBuilder {

    private final Pipeline pipeline;

    public PipeLineBuilder() {
        this.pipeline = new Pipeline();
    }

    public void connectNodes(Node publisher, Node subscriber) {
        pipeline.addNodes(publisher, subscriber);
        Topic topic = assignPublisherTopic(publisher);
        assignSubscriberTopic(subscriber, topic);
        pipeline.addConnection(publisher, subscriber);
    }

    public void run() {
        // Logic to run the pipeline, I think this only makes sense if we start the source.
        // The source is supposed to kickstart the entire pipeline
    }

    private Topic assignPublisherTopic(Node publisher) {
        if (publisher instanceof OperatorNode<?> operatorNode) {
            if (operatorNode.getOutputTopic() == null) {
                String topicName = publisher.getName().replaceAll("[^a-zA-Z0-9._-]", "_") + "_output";
                Topic topic = new Topic(topicName);
                createKafkaTopic(topic.getName()); // Create a new Kafka topic
                operatorNode.setOutputTopic(topic);
                pipeline.addTopic(topic);
                return topic;
            }
            return operatorNode.getOutputTopic();
        }
        return null;
    }

    private void assignSubscriberTopic(Node subscriber, Topic topic) {
        if (subscriber instanceof OperatorNode<?> operatorNode) {
            if (operatorNode instanceof MiningNode<?> miningNode) {
                miningNode.setInputTopic(topic);
            } else if (operatorNode instanceof GenericNode<?> genericNode) {
                genericNode.setInputTopic(topic);
            }
        }
    }

    // We have to create a Kafka topic like this because there is no source publishing to the first topic.
    private void createKafkaTopic(String topicName) {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfiguration.BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(properties)) {
            // Check if topic already exists
            Set<String> existingTopics = adminClient.listTopics().names().get();
            if (existingTopics.contains(topicName)) {
                System.out.println("Topic already exists: " + topicName);
                return;
            }

            // Create the topic
            NewTopic newTopic = new NewTopic(topicName, 1, (short)1);
            CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));

            // Ensure the topic is created before proceeding
            KafkaFuture<Void> future = result.values().get(topicName);
            future.get();  // Wait for completion
            System.out.println("Kafka topic created: " + topicName);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to create Kafka topic: " + e.getMessage());
        }
    }
}
