package service;


import model.Message;
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

    private final Object lock = new Object();
    private volatile boolean isRunning = true;
    private final KafkaConsumer<String, String> kafkaConsumer;

    public Consumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfiguration.BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.kafkaConsumer = new KafkaConsumer<>(props);
    }
    // TODO: add parametrization to InputHandle
    public void subscribe(String topic, InputHandle inputHandle) {
       new Thread(() -> {
            kafkaConsumer.subscribe(List.of(topic));
            try {
                while (true) {
                    synchronized (lock) {
                        while (!isRunning) {
                            lock.wait();
                        }
                    }
                        var records = kafkaConsumer.poll(java.time.Duration.ofMillis(100));

                        if (!records.isEmpty()) {
                            for (ConsumerRecord<String, String> record : records) {
                                if(record.value() != null) {
                                    inputHandle.observe(new Message<>(record.value()));
                                }
                            }
                        }
                    }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                kafkaConsumer.close();
            }
        }).start();
    }

    public void stop() {
        synchronized (lock) {
            isRunning = false;
        }
    }

    public void resume() {
        synchronized (lock) {
            isRunning = true;
            lock.notifyAll();
        }
    }
}
