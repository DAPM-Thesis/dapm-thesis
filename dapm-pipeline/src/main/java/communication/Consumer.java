package communication;

import communication.config.KafkaConfiguration;
import communication.message.Message;
import communication.message.serialization.deserialization.MessageFactory;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaFuture;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Consumer {

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final Subscriber<Message> subscriber;
    private final String topic;
    private final String brokerURL;

    public Consumer(Subscriber<Message> subscriber, String brokerURL, String topic) {
        Properties props = KafkaConfiguration.getConsumerProperties(brokerURL);
        this.kafkaConsumer = new KafkaConsumer<>(props);
        this.subscriber = subscriber;
        this.topic = topic;
        this.brokerURL = brokerURL;
    }

    public void start() {
        final int maxRetries = 10;
        final int retryDelayMillis = 5000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            if (topicExists()) {
                kafkaConsumer.subscribe(List.of(topic));
                observe();
                return;
            }
            System.out.println("Attempt " + attempt + ": Topic '" + topic + "' does not exist. Retrying...");
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        throw new RuntimeException("Failed to find topic '" + topic + "'");
    }

    private void observe() {
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

    private boolean topicExists() {
        Properties props = KafkaConfiguration.getAdminProperties(brokerURL);
        try (AdminClient adminClient = AdminClient.create(props)) {
            ListTopicsResult listTopicsResult = adminClient.listTopics(new ListTopicsOptions().listInternal(false));
            KafkaFuture<Set<String>> topicsFuture = listTopicsResult.names();

            Set<String> topics = topicsFuture.get();
            return topics.contains(topic);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
