package communication;

import message.serialization.MessageSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Producer<T> implements Publisher<T> {

    private KafkaProducer<String, String> kafkaProducer;
    private final String topic;
    private Publisher<T> publisher;

    public Producer(String topic) {
        Properties props = KafkaConfiguration.getProducerProperties();
        this.kafkaProducer = new KafkaProducer<>(props);
        this.topic = topic;
    }

    @Override
    public void publish(T message) {
        MessageSerializer serializer = new MessageSerializer();
        message.acceptVisitor(serializer);
        String serialization = serializer.getSerialization();
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, serialization);
        kafkaProducer.send(record);
    }

    @Override
    public void registerProducer(Publisher<T> producer) {
        this.publisher = producer;
    }

    @Override
    public boolean unsubscribe(Subscriber<T> subscriber) {
        return false;
    }
}
