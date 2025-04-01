package communication;

import communication.message.Message;
import communication.message.serialization.MessageSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Producer {

    private KafkaProducer<String, String> kafkaProducer;
    private final String topic;

    public Producer(String topic) {
        Properties props = KafkaConfiguration.getProducerProperties();
        this.kafkaProducer = new KafkaProducer<>(props);
        this.topic = topic;
    }

    public void publish(Message message) {
        MessageSerializer serializer = new MessageSerializer();
        message.acceptVisitor(serializer);
        String serialization = serializer.getSerialization();
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, serialization);
        kafkaProducer.send(record);
    }
}
