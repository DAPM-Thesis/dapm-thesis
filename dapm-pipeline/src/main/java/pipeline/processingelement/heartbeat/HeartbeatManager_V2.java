package pipeline.processingelement.heartbeat;

import communication.message.Message;
import communication.message.impl.Heartbeat;
import communication.message.serialization.deserialization.MessageFactory;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.apache.kafka.common.errors.GroupNotEmptyException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.reaction.FaultContext;
import pipeline.processingelement.reaction.PeerDirection;
import pipeline.processingelement.reaction.ReactionHandler;
import utils.LogUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HeartbeatManager_V2 { 
    private final ProcessingElement processingElement;
    private final String instanceID;
    private final String brokerUrl;
    private final ReactionHandler reactionHandler; 

    private KafkaProducer<String, String> heartbeatProducer;
    private final String upstreamHeartbeatPublishTopic;
    private final String downstreamHeartbeatPublishTopic;

    private KafkaConsumer<String, String> neighborHeartbeatConsumer;
    private final Set<String> upstreamTopicsToMonitor;
    private final Set<String> downstreamTopicsToMonitor;
    private String generatedNeighborLoggerConsumerGroupId;

    private final ConcurrentMap<String, Instant> lastHeartbeatOnTopic = new ConcurrentHashMap<>();
    private final HeartbeatVerificationStrategy upstreamStrategy = new UpstreamVerificationStrategy();
    private final HeartbeatVerificationStrategy downstreamStrategy = new DownstreamVerificationStrategy();

    private final ScheduledExecutorService scheduler;
    private volatile boolean isRunning = false;

    private static final long HEARTBEAT_SEND_INTERVAL_MS = 3_000;
    private static final long LIVENESS_CHECK_INTERVAL_MS = 5_000;
    private static final long HEARTBEAT_TIMEOUT_MS = 10_000; // Threshold for strategies
    private static final long KAFKA_ADMIN_TIMEOUT_SECONDS = 10;
    private static final long KAFKA_CLIENT_CLOSE_TIMEOUT_SECONDS = 5;
    private volatile boolean verificationGracePeriodOver = false;
    private static final long INITIAL_VERIFICATION_GRACE_PERIOD_MS = 15_000; // e.g., 5 seconds
   


    public HeartbeatManager_V2(
            ProcessingElement processingElement,
            String brokerUrl,
            HeartbeatTopicConfig topicConfig,
            ReactionHandler reactionHandler
    ) {
        this.processingElement = Objects.requireNonNull(processingElement);
        this.instanceID = Objects.requireNonNull(processingElement.getInstanceId());
        this.brokerUrl = Objects.requireNonNull(brokerUrl);
        Objects.requireNonNull(topicConfig, "HeartbeatTopicSetupConfig cannot be null");
        this.reactionHandler = Objects.requireNonNull(reactionHandler, "ReactionHandler cannot be null");

        this.upstreamHeartbeatPublishTopic = topicConfig.getUpstreamHeartbeatPublishTopic();
        this.downstreamHeartbeatPublishTopic = topicConfig.getDownstreamHeartbeatPublishTopic();

        this.upstreamTopicsToMonitor = Collections.unmodifiableSet(new HashSet<>(topicConfig.getUpstreamNeighborHeartbeatTopicsToSubscribeTo()));
        this.downstreamTopicsToMonitor = Collections.unmodifiableSet(new HashSet<>(topicConfig.getDownstreamNeighborHeartbeatTopicsToSubscribeTo()));

        Stream.concat(upstreamTopicsToMonitor.stream(), downstreamTopicsToMonitor.stream())
              .forEach(topic -> lastHeartbeatOnTopic.put(topic, Instant.MIN)); // Initialize all monitored topics

        int numScheduledTasks = 0;
        if (this.upstreamHeartbeatPublishTopic != null || this.downstreamHeartbeatPublishTopic != null) numScheduledTasks++; // Send task
        if (!this.upstreamTopicsToMonitor.isEmpty() || !this.downstreamTopicsToMonitor.isEmpty()) numScheduledTasks++; // Poll/Check task
        this.scheduler = (numScheduledTasks > 0) ? Executors.newScheduledThreadPool(numScheduledTasks) : null;
    }

    private void initializeRawKafkaClients() {
        boolean needsProducer = upstreamHeartbeatPublishTopic != null || downstreamHeartbeatPublishTopic != null;
        boolean needsConsumer = !upstreamTopicsToMonitor.isEmpty() || !downstreamTopicsToMonitor.isEmpty();

        if (needsProducer && heartbeatProducer == null) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            this.heartbeatProducer = new KafkaProducer<>(props);
            LogUtil.info("[HB MANAGER] {} Owner {}: Raw KafkaProducer initialized.", processingElement.getClass().getSimpleName(), instanceID);
        }

        if (needsConsumer && neighborHeartbeatConsumer == null) {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
            this.generatedNeighborLoggerConsumerGroupId = "hb-v2-consumer-" + instanceID + "-" + UUID.randomUUID().toString().substring(0,8);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, this.generatedNeighborLoggerConsumerGroupId);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            this.neighborHeartbeatConsumer = new KafkaConsumer<>(props);
            
            List<String> allTopicsToSubscribe = Stream.concat(upstreamTopicsToMonitor.stream(), downstreamTopicsToMonitor.stream())
                                                      .distinct().collect(Collectors.toList());
            if (!allTopicsToSubscribe.isEmpty()) {
                this.neighborHeartbeatConsumer.subscribe(allTopicsToSubscribe);
                LogUtil.info("[HB MANAGER] {} Owner {}: Raw KafkaConsumer (Group: {}) subscribed to: {}",
                        processingElement.getClass().getSimpleName(), instanceID, this.generatedNeighborLoggerConsumerGroupId, allTopicsToSubscribe);
            } else {
                 LogUtil.info("[HB MANAGER] {} Owner {}: Raw KafkaConsumer initialized (Group: {}) but no topics to monitor.",
                        processingElement.getClass().getSimpleName(), instanceID, this.generatedNeighborLoggerConsumerGroupId);
            }
        }
    }

    private void ensurePublishTopicsExist() {
        List<String> topicsToCreate = new ArrayList<>();
        if (upstreamHeartbeatPublishTopic != null && !upstreamHeartbeatPublishTopic.isEmpty()) topicsToCreate.add(upstreamHeartbeatPublishTopic);
        if (downstreamHeartbeatPublishTopic != null && !downstreamHeartbeatPublishTopic.isEmpty()) topicsToCreate.add(downstreamHeartbeatPublishTopic);

        if (topicsToCreate.isEmpty()) return;

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            Set<String> existingTopics = adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
            List<NewTopic> newTopics = new ArrayList<>();
            for (String topic : topicsToCreate) {
                if (!existingTopics.contains(topic)) {
                    newTopics.add(new NewTopic(topic, 1, (short) 1));
                }
            }
            if (!newTopics.isEmpty()) {
                adminClient.createTopics(newTopics).all().get(5, TimeUnit.SECONDS);
                LogUtil.info("[HB TOPIC] {} ProcessingElement {}: Created own publish topics: {}", processingElement.getClass().getSimpleName(), instanceID, newTopics.stream().map(NewTopic::name).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            LogUtil.error(e, "[HB] {} Owner {}: Failed to ensure own publish topics exist", processingElement.getClass().getSimpleName(), instanceID);
        }
    }
    
    public synchronized void start() {
        if (instanceID == null) { /* ... error log ... */ return; }
        if (isRunning || scheduler == null) { /* ... logging ... */ if(scheduler == null) isRunning = true; return; }
        LogUtil.info("[HB MANAGER] {} Owner {}: Starting...", processingElement.getClass().getSimpleName(), instanceID);
        isRunning = true;

        initializeRawKafkaClients(); // Sets up generatedConsumerGroupId
        ensurePublishTopicsExist();

        // Schedule grace period completion
        if (scheduler != null && (!upstreamTopicsToMonitor.isEmpty() || !downstreamTopicsToMonitor.isEmpty())) {
            scheduler.schedule(() -> {
                LogUtil.info("[HB MANAGER] {} ProcessingElement {}: Initial verification grace period ended.", processingElement.getClass().getSimpleName(), instanceID);
                verificationGracePeriodOver = true;
            }, INITIAL_VERIFICATION_GRACE_PERIOD_MS, TimeUnit.MILLISECONDS);
        } else if (scheduler == null) { // No tasks, no checks needed
            verificationGracePeriodOver = true; 
        }

        // Publish Loop
        if (heartbeatProducer != null && (upstreamHeartbeatPublishTopic != null || downstreamHeartbeatPublishTopic != null)) {
            scheduler.scheduleWithFixedDelay(() -> {
                if (!isRunning || !processingElement.isAvailable()) return;
                try {
                    Heartbeat heartbeat = new Heartbeat(instanceID, Instant.now());
                    String serializedHeartbeat = heartbeat.getName() + ":" + heartbeat.getPayloadAsJson();

                    if (upstreamHeartbeatPublishTopic != null) {
                        heartbeatProducer.send(new ProducerRecord<>(upstreamHeartbeatPublishTopic, serializedHeartbeat));
                         LogUtil.info("[HB SEND] {} Owner {}: Sent heartbeat to UPSTREAM topic {}", processingElement.getClass().getSimpleName(), instanceID, upstreamHeartbeatPublishTopic);
                    }
                    if (downstreamHeartbeatPublishTopic != null) {
                        heartbeatProducer.send(new ProducerRecord<>(downstreamHeartbeatPublishTopic, serializedHeartbeat));
                        LogUtil.info("[HB SEND] {} Owner {}: Sent heartbeat to DOWNSTREAM topic {}", processingElement.getClass().getSimpleName(), instanceID, downstreamHeartbeatPublishTopic);                    
                    }
                } catch (Exception e) { LogUtil.error(e, "[HB MANAGER] Error sending heartbeat for {}", instanceID);}
            }, ThreadLocalRandom.current().nextInt(200, 700), HEARTBEAT_SEND_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }

        // Poll for neighbor pulses & Check Liveness Loop
        List<String> allTopicsToSubscribe = Stream.concat(upstreamTopicsToMonitor.stream(), downstreamTopicsToMonitor.stream())
                                                 .distinct().collect(Collectors.toList());

        if (neighborHeartbeatConsumer != null && !allTopicsToSubscribe.isEmpty()) {
             neighborHeartbeatConsumer.subscribe(allTopicsToSubscribe); // Ensure subscription is set if re-starting
            scheduler.scheduleWithFixedDelay(() -> {
                if (!isRunning || !processingElement.isAvailable()) return; // Also check owner availability before reacting
                try {
                    // 1. Poll for incoming pulses
                    ConsumerRecords<String, String> records = neighborHeartbeatConsumer.poll(Duration.ofMillis(200)); // Short poll, check runs often
                    Instant now = Instant.now();
                    records.forEach(record -> {
                        Message deserialized = MessageFactory.deserialize(record.value());
                        if (deserialized instanceof Heartbeat heartbeat) {
                            LogUtil.info("[HB MANAGER RECV] {} received heartbeat from {} on topic {}", processingElement.getClass(), instanceID, heartbeat.getInstanceID(), record.topic());
                            lastHeartbeatOnTopic.put(record.topic(), heartbeat.getTimestamp()); // Use message timestamp
                        }
                    });

                    if(!verificationGracePeriodOver) { return;}

                    // 2. Verify Upstream Peers (based on their topics)
                    if (!upstreamTopicsToMonitor.isEmpty()) {
                        if (!upstreamStrategy.verifyLiveness(new HashMap<>(lastHeartbeatOnTopic), now, HEARTBEAT_TIMEOUT_MS, upstreamTopicsToMonitor)) {
                            LogUtil.info("[HB MANAGER FAULT] {} Owner {}: Upstream liveness check FAILED.", processingElement.getClass().getSimpleName(), instanceID);
                            Set<String> silentUpstreamTopics = upstreamTopicsToMonitor.stream()
                                .filter(topic -> !upstreamStrategy.isTopicTimely(lastHeartbeatOnTopic.getOrDefault(topic, Instant.MIN), now, HEARTBEAT_TIMEOUT_MS))
                                .collect(Collectors.toSet());
                            reactionHandler.processLivenessFailure(new FaultContext(PeerDirection.UPSTREAM_PRODUCER, silentUpstreamTopics, upstreamTopicsToMonitor));
                            // If reaction handler doesn't stop the PE/manager, this will keep firing.
                            // The reaction handler for severe levels should ensure processingElement.setAvailable(false) or stop this manager.
                        }
                    }

                    // 3. Verify Downstream Peers (based on their topics)
                    if (!downstreamTopicsToMonitor.isEmpty()) {
                        if (!downstreamStrategy.verifyLiveness(new HashMap<>(lastHeartbeatOnTopic), now, HEARTBEAT_TIMEOUT_MS, downstreamTopicsToMonitor)) {
                            LogUtil.info("[HB MANAGER FAULT] {} Owner {}: Downstream liveness check FAILED.", processingElement.getClass().getSimpleName(), instanceID);
                             Set<String> silentDownstreamTopics = downstreamTopicsToMonitor.stream()
                                .filter(topic -> !downstreamStrategy.isTopicTimely(lastHeartbeatOnTopic.getOrDefault(topic, Instant.MIN), now, HEARTBEAT_TIMEOUT_MS))
                                .collect(Collectors.toSet());
                            reactionHandler.processLivenessFailure(new FaultContext(PeerDirection.DOWNSTREAM_CONSUMER, silentDownstreamTopics, downstreamTopicsToMonitor));
                        }
                    }
                } catch (Exception e) { if (isRunning) LogUtil.error(e, "[HB MANAGER] Error in poll/check loop for {}", instanceID);}
            }, LIVENESS_CHECK_INTERVAL_MS, LIVENESS_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
        LogUtil.info("[HB MANAGER] {} Owner {}: Started successfully.", processingElement.getClass().getSimpleName(), instanceID);
    }

    private void deleteOwnPublishTopics() {
        List<String> topicsToDelete = new ArrayList<>();
        if (upstreamHeartbeatPublishTopic != null && !upstreamHeartbeatPublishTopic.isEmpty()) {
            topicsToDelete.add(upstreamHeartbeatPublishTopic);
        }
        if (downstreamHeartbeatPublishTopic != null && !downstreamHeartbeatPublishTopic.isEmpty()) {
            topicsToDelete.add(downstreamHeartbeatPublishTopic);
        }

        if (topicsToDelete.isEmpty()) {
            return;
        }

        LogUtil.info("[HB MANAGER] {} Owner {}: Attempting to delete own publish topics: {}", processingElement.getClass().getSimpleName(), instanceID, topicsToDelete);
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            Set<String> existingTopics = adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
            List<String> topicsThatActuallyExist = topicsToDelete.stream()
                    .filter(existingTopics::contains)
                    .collect(Collectors.toList());

            if (!topicsThatActuallyExist.isEmpty()) {
                DeleteTopicsResult deleteResult = adminClient.deleteTopics(topicsThatActuallyExist);
                deleteResult.all().get(10, TimeUnit.SECONDS); // Wait for deletion
                LogUtil.info("[HB MANAGER] {} Owner {}: Successfully deleted own heartbeat publish topics: {}",
                             processingElement.getClass().getSimpleName(), instanceID, topicsThatActuallyExist);
            } else {
                LogUtil.info("[HB MANAGER] {} Owner {}: Own publish topics {} did not exist or were already deleted.",
                             processingElement.getClass().getSimpleName(), instanceID, topicsToDelete);
            }        
        } catch (Exception e) { // Catch any other Kafka or admin client related exceptions
            LogUtil.error(e, "[HB MANAGER ERR] {} Owner {}: Unexpected error deleting topics {}", processingElement.getClass().getSimpleName(), instanceID, topicsToDelete);
        }
    }

    private void deleteOwnConsumerGroup() {
        if (generatedNeighborLoggerConsumerGroupId == null || generatedNeighborLoggerConsumerGroupId.isEmpty()) {
            LogUtil.info("[HB MANAGER] {} Owner {}: No consumer group ID stored to delete.", processingElement.getClass().getSimpleName(), instanceID);
            return;
        }

        LogUtil.info("[HB MANAGER] {} Owner {}: Attempting to delete own consumer group: {}", processingElement.getClass().getSimpleName(), instanceID, generatedNeighborLoggerConsumerGroupId);
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            DeleteConsumerGroupsResult deleteResult = adminClient.deleteConsumerGroups(Collections.singletonList(generatedNeighborLoggerConsumerGroupId));
            deleteResult.all().get(KAFKA_ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            LogUtil.info("[HB MANAGER] {} Owner {}: Successfully deleted own consumer group: {}",
                         processingElement.getClass().getSimpleName(), instanceID, generatedNeighborLoggerConsumerGroupId);
        } catch (TimeoutException e) {
            LogUtil.error(e, "[HB MANAGER] {} Owner {}: Timeout deleting consumer group {}", processingElement.getClass().getSimpleName(), instanceID, generatedNeighborLoggerConsumerGroupId);
        } catch (InterruptedException e) {
            LogUtil.error(e, "[HB MANAGER] {} Owner {}: Interrupted deleting consumer group {}", processingElement.getClass().getSimpleName(), instanceID, generatedNeighborLoggerConsumerGroupId);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof GroupIdNotFoundException) {
                LogUtil.info("[HB MANAGER] {} Owner {}: Consumer group {} not found (possibly already deleted or never fully formed).", processingElement.getClass().getSimpleName(), instanceID, generatedNeighborLoggerConsumerGroupId);
            } else if (e.getCause() instanceof GroupNotEmptyException) {
                 LogUtil.info("[HB MANAGER] {} Owner {}: Consumer group {} is not empty. Cannot delete. Ensure consumer was fully closed and timed out.", processingElement.getClass().getSimpleName(), instanceID, generatedNeighborLoggerConsumerGroupId);
            } else {
                LogUtil.error(e, "[HB MANAGER] {} Owner {}: ExecutionException deleting consumer group {}", processingElement.getClass().getSimpleName(), instanceID, generatedNeighborLoggerConsumerGroupId);
            }
        } catch (Exception e) {
             LogUtil.error(e, "[HB MANAGER] {} Owner {}: Unexpected error deleting consumer group {}", processingElement.getClass().getSimpleName(), instanceID, generatedNeighborLoggerConsumerGroupId);
        }
    }

    public synchronized void stop() {
        if (!isRunning) {
            LogUtil.info("[HB MANAGER] {} Owner {}: Already stopped or not started.", processingElement.getClass().getSimpleName(), instanceID);
            return;
        }
        LogUtil.info("[HB MANAGER] {} Owner {}: Stopping...", processingElement.getClass().getSimpleName(), instanceID);
        isRunning = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(HEARTBEAT_SEND_INTERVAL_MS + 1000, TimeUnit.MILLISECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (heartbeatProducer != null) {
            try {
                heartbeatProducer.flush();
                heartbeatProducer.close(Duration.ofSeconds(KAFKA_CLIENT_CLOSE_TIMEOUT_SECONDS));
            } catch (Exception e) {LogUtil.error(e, "[HB MANAGER] Error closing heartbeatProducer for {}", instanceID);}
            heartbeatProducer = null;
        }
        
        if (neighborHeartbeatConsumer != null) {
            try {
                neighborHeartbeatConsumer.unsubscribe();
                neighborHeartbeatConsumer.wakeup();
                neighborHeartbeatConsumer.close(Duration.ofSeconds(KAFKA_CLIENT_CLOSE_TIMEOUT_SECONDS));
            } catch (Exception e) {LogUtil.error(e, "[HB MANAGER] Error closing neighborHeartbeatLoggerConsumer for {}", instanceID);}
            neighborHeartbeatConsumer = null; 
        }

        deleteOwnPublishTopics();
        deleteOwnConsumerGroup();

        LogUtil.info("[HB MANAGER] {} Owner {}: Stopped.", processingElement.getClass().getSimpleName(), instanceID);
    }
}
