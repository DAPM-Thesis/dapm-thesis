package communication;

import communication.config.KafkaConfiguration;
import communication.config.ProducerConfig;
import communication.message.Message;
import communication.message.serialization.MessageSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.admin.AdminClient;

import java.util.Collections;
import java.util.Properties;


public class Producer {

    private KafkaProducer<String, String> kafkaProducer;
    private final String topic;

    public Producer(ProducerConfig config) {
        Properties props = KafkaConfiguration.getProducerProperties(config.brokerURL());
        this.kafkaProducer = new KafkaProducer<>(props);
        this.topic = config.topic();

        createKafkaTopicIfNotExist(config.brokerURL());
    }

    public void publish(Message message) {
        // TODO: make serializer static so a new one is not created with every publish
        // TODO: only make it static if it does not cause issues when run concurrently (because of getSerialization()) - remodel Serializer if concurrency becomes an issue.
        MessageSerializer serializer = new MessageSerializer();
        message.acceptVisitor(serializer);
        String serialization = serializer.getSerialization();
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, serialization);
        kafkaProducer.send(record); // TODO: inline ProducerRecord?
    }

    private void createKafkaTopicIfNotExist(String brokerURL) {
        Properties adminProps = KafkaConfiguration.getAdminProperties(brokerURL);
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            // Check if the topic already exists
            if (!adminClient.listTopics().names().get().contains(topic)) {
                // Create a new topic with a default number of partitions and replication factor
                NewTopic newTopic = new NewTopic(topic, 1, (short) 1); // 1 partition, replication factor of 1
                adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
                System.out.println("Topic created: " + topic); // TODO: do we want the print?
            } else {
                System.out.println("Topic already exists: " + topic); // TODO: is this ever called? If it is, do we want any logic around it?
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
