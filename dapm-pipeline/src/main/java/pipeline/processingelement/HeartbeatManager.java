package pipeline.processingelement;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import communication.ProducingProcessingElement;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class HeartbeatManager {
    private final ProcessingElement owner;
    private final KafkaProducer<String,String> producer;
    private final KafkaConsumer<String,String> consumer;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // instanceId -> last heartbeat timestamp
    private final ConcurrentMap<String, Instant> lastSeen = new ConcurrentHashMap<>();
    private final List<PeerLink> links = new CopyOnWriteArrayList<>();

    private static final long HEARTBEAT_INTERVAL_MS = 3_000;
    private static final long HEARTBEAT_TIMEOUT_MS  = 10_000;

    private final Set<String> subscribedTopics = ConcurrentHashMap.newKeySet();


    public HeartbeatManager(ProcessingElement owner, String brokerUrl) {
        this.owner = owner;

        var prodProps = new Properties();
        prodProps.put("bootstrap.servers", brokerUrl);
        prodProps.put("key.serializer",   StringSerializer.class.getName());
        prodProps.put("value.serializer", StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(prodProps);

        var consProps = new Properties();
        consProps.put("bootstrap.servers",        brokerUrl);
        consProps.put("group.id",                 "hb-"+ owner.getInstanceId());
        consProps.put("key.deserializer",         StringDeserializer.class.getName());
        consProps.put("value.deserializer",       StringDeserializer.class.getName());
        consProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        this.consumer = new KafkaConsumer<>(consProps);
    }

    public void addLink(String instanceId, String sendTopic, String receiveTopic) {
        if (instanceId == null || receiveTopic == null) {
            System.err.println("[HB] " + owner.getClass() + ": cannot add link with null instanceId or receiveTopic");
            return;
        }
        links.add(new PeerLink(instanceId, sendTopic, receiveTopic));
        lastSeen.put(instanceId, Instant.MIN);
        consumer.subscribe(List.of(receiveTopic));
        subscribedTopics.add(receiveTopic);
        System.out.println("[HB] " + owner.getClass() + owner.getInstanceId() + ": subscribing to heartbeat topic " + receiveTopic + " for peer " + instanceId);
    }

    public void start() {
        consumer.subscribe(subscribedTopics);
        // Poll loop
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                ConsumerRecords<String,String> recs = consumer.poll(Duration.ofMillis(500));
                recs.forEach(r -> {
                    String from;
                    String val = r.value();
                    if (val != null && val.startsWith("hb:")) {
                        from = val.substring(3);
                    } else {
                        System.err.printf("[HB] %s ignoring malformed hb payload '%s'%n",
                            owner.getInstanceId(), val);
                        return;
                    }
                    lastSeen.put(from, Instant.now());
                    System.out.printf("[HB] %s heartbeat received from %s on topic %s%n",
                        owner.getInstanceId(), from, r.topic());
                });
            } catch (Throwable t) {
                System.err.printf("[HB] %s poll loop error: %s%n",
                    owner.getInstanceId(), t.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);

        // Send loop
        scheduler.scheduleWithFixedDelay(() -> {
            if (!owner.isAvailable()) return;
            String msg = "hb:" + owner.getInstanceId();
            for (var l : links) {
                producer.send(new ProducerRecord<>(l.sendTopic, msg));
                System.out.println("[HB] " + owner.getClass() + ": sending heartbeat to " + l.instanceId + " on topic " + l.sendTopic);                
            }
        }, 0, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);

        // Timeout loop
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                Instant now = Instant.now();
                for (var l : links) {
                    Instant last = lastSeen.getOrDefault(l.instanceId, Instant.MIN);
                    if (Duration.between(last, now).toMillis() > HEARTBEAT_TIMEOUT_MS) {
                        onMissedHeartbeat(l.instanceId);
                    }
                }
            } catch (Throwable t) {
                System.err.printf("[HB] %s timeout loop error: %s%n",
                    owner.getInstanceId(), t.getMessage());
            }
        }, HEARTBEAT_INTERVAL_MS, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
        consumer.unsubscribe();
        consumer.wakeup();
        consumer.close();
        producer.close();
    }

    private void onMissedHeartbeat(String deadPeer) {
        System.err.printf("[HB] %s missed heartbeat from %s%n",
            owner.getInstanceId(), deadPeer);
        if (owner instanceof ConsumingProcessingElement) {
            System.err.printf("[HB] %s upstream %s dead → terminating%n", owner.getInstanceId(), deadPeer);
            owner.terminate();
        } else if (owner instanceof ProducingProcessingElement) {
            boolean anyAlive = links.stream()
                .anyMatch(l -> Duration.between(
                    lastSeen.getOrDefault(l.instanceId, Instant.MIN),
                    Instant.now())
                  .toMillis() <= HEARTBEAT_TIMEOUT_MS);
            if (!anyAlive) {
                System.err.printf("[HB] %s no downstream alive → terminating%n", owner.getInstanceId());
                owner.terminate();
            }
        }
    }

    private static record PeerLink(String instanceId, String sendTopic, String receiveTopic) {}
}
