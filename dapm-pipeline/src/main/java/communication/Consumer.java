package communication;

import communication.message.Message;
import communication.message.serialization.deserialization.MessageFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.List;
import java.util.Properties;

public class Consumer {

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final Subscriber<Message> subscriber;

    public Consumer(Subscriber<Message> subscriber, String topic) {
        Properties props = KafkaConfiguration.getConsumerProperties();
        this.kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(List.of(topic));
        this.subscriber = subscriber;
        observe();
    }

    public void observe() {
        new Thread(() -> {
            while (true) {
                var records = kafkaConsumer.poll(java.time.Duration.ofMillis(100));

                if (!records.isEmpty()) {
                    for (ConsumerRecord<String, String> record : records) {
                        Message msg =  MessageFactory.deserialize(record.value());
                        this.subscriber.observe(msg);
                    }
                }
            }
        }).start();
    }
}
