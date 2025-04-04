package communication;

import communication.message.Message;
import communication.message.serialization.MessageSerializer;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collections;
import java.util.Properties;


public class Producer {

    private KafkaProducer<String, String> kafkaProducer;
    private final String topic;

    public Producer(String topic, String brokerURL) {
        Properties props = KafkaConfiguration.getProducerProperties(brokerURL);
        this.kafkaProducer = new KafkaProducer<>(props);
        this.topic = topic;

        createKafkaTopicIfNotExist(brokerURL);
    }

    public void publish(Message message) {
        MessageSerializer serializer = new MessageSerializer();
        message.acceptVisitor(serializer);
        String serialization = serializer.getSerialization();
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, serialization);
        kafkaProducer.send(record);
    }

    private void createKafkaTopicIfNotExist(String brokerURL) {
        Properties adminProps = KafkaConfiguration.getAdminProperties(brokerURL);
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            // Check if the topic already exists
            if (!adminClient.listTopics().names().get().contains(topic)) {
                // Create a new topic with a default number of partitions and replication factor
                NewTopic newTopic = new NewTopic(topic, 1, (short) 1); // 1 partition, replication factor of 1
                adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
                System.out.println("Topic created: " + topic);
            } else {
                System.out.println("Topic already exists: " + topic);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
