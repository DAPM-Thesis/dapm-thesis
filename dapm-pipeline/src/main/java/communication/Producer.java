package communication;

import communication.config.ProducerConfig;
import communication.message.Message;
import communication.message.serialization.MessageSerializer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import utils.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@Component
@Scope("prototype")
public class Producer {

    private KafkaProducer<String, String> kafkaProducer;
    private String topic;
    private String brokerUrl;

    @Autowired
    public Producer() {
    }

    public void registerPublisher(ProducerConfig producerConfig) {
        this.topic = producerConfig.topic();
        this.brokerUrl = producerConfig.brokerURL();
        createKafkaTopic(topic, brokerUrl);
        this.kafkaProducer = createProducer(producerConfig.brokerURL());
    }

    public void publish(Message message) {
        MessageSerializer serializer = new MessageSerializer();
        message.acceptVisitor(serializer);
        String serialization = serializer.getSerialization();
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, serialization);
        try {
            kafkaProducer.send(record);
        } catch (Exception e) {
            throw new KafkaException("Failed to send record to topic " + topic, e);
        }
    }

    public boolean terminate() {
        try {
            kafkaProducer.flush();
            kafkaProducer.close();
            kafkaProducer = null;
            deleteKafkaTopic(brokerUrl, topic);
            return true;
        } catch (Exception e) {
            LogUtil.error(e, "Failed to close Kafka producer for topic {} ", topic);
            return false;
        }
    }

    private  KafkaProducer<String, String> createProducer(String brokerUrl) {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    private void createKafkaTopic(String topic, String brokerURL) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerURL);
        try (AdminClient adminClient = AdminClient.create(props)) {
            if (!adminClient.listTopics().names().get().contains(topic)) {
                NewTopic newTopic = new NewTopic(topic, 1, (short) 1);
                adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            } else {
                LogUtil.debug("Topic {} already exists.", topic);
            }
        } catch (Exception e) {
            throw new KafkaException("Failed to create topic, " + topic, e);
        }
    }

    private void deleteKafkaTopic(String brokerURL, String topic) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerURL);
        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.deleteTopics(Collections.singletonList(topic)).all().get();
        } catch (Exception e) {
            throw new KafkaException("Failed to delete topic, " + topic, e);
        }
    }
}

