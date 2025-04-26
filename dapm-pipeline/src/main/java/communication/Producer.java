package communication;

import communication.config.KafkaConfiguration;
import communication.config.ProducerConfig;
import communication.message.Message;
import communication.message.serialization.MessageSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.admin.AdminClient;
import utils.LogUtil;

import java.util.Collections;
import java.util.Properties;


public class Producer {

    private KafkaProducer<String, String> kafkaProducer;
    private final String topic;
    private final String brokerURL;

    public Producer(ProducerConfig config) {
        Properties props = KafkaConfiguration.getProducerProperties(config.brokerURL());
        this.kafkaProducer = new KafkaProducer<>(props);
        this.topic = config.topic();
        this.brokerURL = config.brokerURL();
        createKafkaTopicIfNotExist(config.brokerURL());
    }

    public boolean terminate() {
        try {
            deleteKafkaTopic();
            kafkaProducer.close();
            LogUtil.info("Kafka producer closed for topic {} ", topic);
            return true;
        } catch (Exception e) {
            LogUtil.error(e, "Failed to close Kafka producer for topic {} ", topic);
            return false;
        }
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
            if (!adminClient.listTopics().names().get().contains(topic)) {
                NewTopic newTopic = new NewTopic(topic, 1, (short) 1);
                adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
                LogUtil.info("Topic created: " + topic);
            } else {
                LogUtil.info("Topic already exists: " + topic);
            }
        }catch (Exception e) {
            LogUtil.error(e, "Failed to create topic, {}", topic);
        }
    }

    private void deleteKafkaTopic() throws Exception {
        Properties adminProps = KafkaConfiguration.getAdminProperties(brokerURL);
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            adminClient.deleteTopics(Collections.singletonList(topic)).all().get();
            LogUtil.info("Topic deleted: " + topic);
        }
    }
}