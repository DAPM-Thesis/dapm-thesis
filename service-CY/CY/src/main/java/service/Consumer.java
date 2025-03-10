package service;

import main.Message;
import node.handle.InputHandle;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
public class Consumer {

    private final KafkaConsumer<String, String> kafkaConsumer;

    public Consumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.kafkaConsumer = new KafkaConsumer<>(props);
    }

    public void subscribe(String topic, InputHandle inputHandle) {
        new Thread(() -> {
            kafkaConsumer.subscribe(List.of(topic));
            while (true) {
                var records = kafkaConsumer.poll(java.time.Duration.ofMillis(100));

                if (!records.isEmpty()) {
                    for (ConsumerRecord<String, String> record : records) {
                        inputHandle.observe(new Message<>(record.value()));
                    }
                }
            }
        }).start();
    }
}
