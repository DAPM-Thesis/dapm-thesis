package service;


import datatype.DataType;
import model.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class Producer<T> {

    private final KafkaProducer<String, Message<T>> kafkaProducer;

    public Producer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.kafkaProducer = new KafkaProducer<>(props);
    }

    public void publish(String topic, Message<T> message) {
        ProducerRecord<String, Message<T>> record = new ProducerRecord<String, Message<T>>(topic, message);
        this.kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
            }
            else {
                System.out.println("Message is published to: " + metadata.topic());
            }
        });
    }
}
