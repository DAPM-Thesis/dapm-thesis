package communication;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.List;
import java.util.Properties;

public class Consumer<T> implements Subscriber<T> {

    private final KafkaConsumer<String, T> kafkaConsumer;
    private Subscriber<T> subscriber;

    public Consumer(String topic) {
        Properties props = KafkaConfiguration.getConsumerProperties();
        this.kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(List.of(topic));
    }

    @Override
    public void observe(T t) {
        new Thread(() -> {
            while (true) {
                var records = kafkaConsumer.poll(java.time.Duration.ofMillis(100));

                if (!records.isEmpty()) {
                    for (ConsumerRecord<String, T> record : records) {
                        this.subscriber.observe(record.value());
                    }
                }
            }
        }).start();
    }

    @Override
    public void registerConsumer(Subscriber<T> subscriber) {
        this.subscriber = subscriber;
    }
}
