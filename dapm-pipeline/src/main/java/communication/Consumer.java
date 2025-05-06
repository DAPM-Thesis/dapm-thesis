package communication;

import communication.config.ConsumerConfig;
import communication.config.KafkaConfiguration;
import communication.message.Message;
import communication.message.serialization.deserialization.MessageFactory;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import utils.LogUtil;

import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Consumer {

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final Subscriber<Message> subscriber;
    private final String topic;
    private final String brokerURL;
    private final int portNumber;

    private volatile boolean running;
    private Thread thread;

    public Consumer(Subscriber<Message> subscriber, ConsumerConfig config) {
        Properties props = KafkaConfiguration.getConsumerProperties(config.brokerURL());
        this.kafkaConsumer = new KafkaConsumer<>(props);
        this.subscriber = subscriber;
        this.topic = config.topic();
        this.brokerURL = config.brokerURL();
        this.portNumber = config.portNumber();
        subscribeToTopic();
    }

    public void start() {
        if (thread != null && thread.isAlive()) {
            LogUtil.debug("Consumer already running for topic {} ", topic);
            return;
        }
        running = true;
        try {
            observe();
        } catch (Exception e) {
            running = false;
            throw new KafkaException("Failed to start consumer for topic " + topic, e);
        }
    }

    public void pause() {
        running = false;
        kafkaConsumer.wakeup();
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new KafkaException("Interrupted while stopping consumer for topic " + topic, e);
            } finally {
                thread = null;
            }
        }
    }

    public void terminate() {
        try {
            pause();
            kafkaConsumer.close();
        } catch (Exception e) {
            throw new KafkaException("Failed to close Kafka consumer for topic " + topic, e);
        }
    }

    private void observe() {
        thread = new Thread(() -> {
            try {
                while (running) {
                    var records = kafkaConsumer.poll(java.time.Duration.ofMillis(100));
                    if (!records.isEmpty()) {
                        try {
                            for (ConsumerRecord<String, String> record : records) {
                                Message msg = MessageFactory.deserialize(record.value());
                                this.subscriber.observe(msg, portNumber);
                            }
                        } catch (Exception e) {
                            LogUtil.error(e, "Failed to process record in consumer for topic {} ", topic);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e, "Failed to process record in consumer for topic {} ", topic);
            }
        }, "KafkaConsumer-" + topic);
        thread.start();
    }

    private void subscribeToTopic() {
        final int maxRetries = 10;
        final int retryDelayMillis = 5000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            if (topicExists()) {
                kafkaConsumer.subscribe(List.of(topic));
                return;
            }
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new KafkaException("Interrupted while waiting for topic '" + topic + "' to exist.", e);
                }
            }
        }
        throw new IllegalStateException("Failed to find topic '" + topic + "' after " + maxRetries + " attempts.");
    }

    private boolean topicExists() {
        Properties props = KafkaConfiguration.getAdminProperties(brokerURL);
        try (AdminClient adminClient = AdminClient.create(props)) {
            Set<String> topics = adminClient
                    .listTopics(new ListTopicsOptions().listInternal(false))
                    .names()
                    .get();
            return topics.contains(topic);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaException("Interrupted while checking topic existence " + topic, e);
        } catch (Exception e) {
            throw new KafkaException("Failed to get list of topics for broker " + brokerURL, e);
        }
    }
}
