package communication;

import communication.config.ProducerConfig;
import communication.message.Message;
import communication.message.serialization.MessageSerializer;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import utils.LogUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
@Scope("prototype")
public class Producer implements Publisher<Message> {

    private KafkaProducer<String, String> kafkaProducer;
    private String topic;
    private String brokerUrl;
    private volatile boolean pausePublishing = false;

    public void registerPublisher(ProducerConfig producerConfig) {
        this.topic = producerConfig.topic();
        this.brokerUrl = producerConfig.brokerURL();
        createKafkaTopic();
        this.kafkaProducer = createProducer();
    }

    @Override
    public void publish(Message message) {
        if (pausePublishing) return;
        MessageSerializer serializer = new MessageSerializer();
        message.acceptVisitor(serializer);
        String serialization = serializer.getSerialization();
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, serialization);
        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                LogUtil.error(exception, "Failed to publish message '{}'", message, exception);
            }
        });
    }

    public boolean pause() {
        try {
            pausePublishing = true; // Pause publishing while emptying topics
            kafkaProducer.flush();
            emptyTopic();
            pausePublishing = false;
            return true;
        } catch (Exception e) {
            LogUtil.error(e, "Failed to pause Kafka producer for topic {} ", topic);
            return false;
        }
    }

    public boolean terminate() {
        try {
            kafkaProducer.flush();
            kafkaProducer.close();
            kafkaProducer = null;
            deleteKafkaTopic();
            return true;
        } catch (Exception e) {
            LogUtil.error(e, "Failed to close Kafka producer for topic {} ", topic);
            return false;
        }
    }

    private KafkaProducer<String, String> createProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    private void createKafkaTopic() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            if (!adminClient.listTopics().names().get(5, TimeUnit.SECONDS).contains(topic)) {
                NewTopic newTopic = new NewTopic(topic, 1, (short) 1);
                adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            }
        } catch (Exception e) {
            throw new KafkaException("Failed to create topic, " + topic, e);
        }
    }

    private void deleteKafkaTopic() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.deleteTopics(Collections.singletonList(topic)).all().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new KafkaException("Failed to delete topic, " + topic, e);
        }
    }

    private void emptyTopic() {
        deleteKafkaTopic();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaException("Failed to empty topic, " + topic, e);
        }
        createKafkaTopic();
    }
}

