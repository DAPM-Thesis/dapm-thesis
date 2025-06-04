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

    private KafkaConsumer<String, String> heartbeatConsumer;
    private final Set<String> upstreamTopicsToMonitor;
    private final Set<String> downstreamTopicsToMonitor;
    private String heartbeatConsumerGroupId;

    private final ConcurrentMap<String, Instant> lastHeartbeatOnTopic = new ConcurrentHashMap<>();
    private final HeartbeatVerificationStrategy upstreamStrategy;
    private final HeartbeatVerificationStrategy downstreamStrategy;

    private final ScheduledExecutorService scheduler;
    private volatile boolean isRunning = false;

    // THRESHOLDS AND INTERVALS
    private static final long HEARTBEAT_SEND_INTERVAL_MS = 3_000;
    private static final long LIVENESS_CHECK_INTERVAL_MS = 5_000;
    private static final long HEARTBEAT_TIMEOUT_MS = 10_000; // Threshold for strategies
    private static final long KAFKA_ADMIN_TIMEOUT_SECONDS = 10;
    private static final long KAFKA_CLIENT_CLOSE_TIMEOUT_SECONDS = 5;

    private volatile boolean verificationGracePeriodOver = false;
    private static final long INITIAL_VERIFICATION_GRACE_PERIOD_MS = 30_000; //30 seconds
   


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

        // Initialize verfication strategies based on fault tolerance level
        this.upstreamStrategy = new UpstreamVerificationStrategy(); // Currently we want all upstream PE instances to be active

        if(processingElement.getFaultToleranceLevel() == FaultToleranceLevel.LEVEL_TERMINATE_ENTIRE_PIPELINE ||
            processingElement.getFaultToleranceLevel() == FaultToleranceLevel.LEVEL_NOTIFY_ONLY) {
            this.downstreamStrategy = new AllDownstreamTopicsActiveStrategy();
        }
        else{
            this.downstreamStrategy = new AnyDownstreamVerificationStrategy();
        }

        this.upstreamHeartbeatPublishTopic = topicConfig.getUpstreamHeartbeatPublishTopic();
        this.downstreamHeartbeatPublishTopic = topicConfig.getDownstreamHeartbeatPublishTopic();

        this.upstreamTopicsToMonitor = Collections.unmodifiableSet(new HashSet<>(topicConfig.getUpstreamNeighborHeartbeatTopicsToSubscribeTo()));
        this.downstreamTopicsToMonitor = Collections.unmodifiableSet(new HashSet<>(topicConfig.getDownstreamNeighborHeartbeatTopicsToSubscribeTo()));

        Stream.concat(upstreamTopicsToMonitor.stream(), downstreamTopicsToMonitor.stream())
              .forEach(topic -> lastHeartbeatOnTopic.put(topic, Instant.MIN)); 

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
            LogUtil.info("[HB MANAGER] {} Processing Element {}: Raw KafkaProducer initialized.", processingElement.getClass().getSimpleName(), instanceID);
        }

        if (needsConsumer && heartbeatConsumer == null) {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
            this.heartbeatConsumerGroupId = "hb-consumer-" + instanceID + "-" + UUID.randomUUID().toString().substring(0,8);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, this.heartbeatConsumerGroupId);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            this.heartbeatConsumer = new KafkaConsumer<>(props);
            
            List<String> allTopicsToSubscribe = Stream.concat(upstreamTopicsToMonitor.stream(), downstreamTopicsToMonitor.stream())
                                                      .distinct().collect(Collectors.toList());
            if (!allTopicsToSubscribe.isEmpty()) {
                this.heartbeatConsumer.subscribe(allTopicsToSubscribe);
                LogUtil.info("[HB MANAGER] {} Processing Element {}: Raw KafkaConsumer (Group: {}) subscribed to: {}",
                        processingElement.getClass().getSimpleName(), instanceID, this.heartbeatConsumerGroupId, allTopicsToSubscribe);
            } else {
                 LogUtil.info("[HB MANAGER] {} Processing Element {}: Raw KafkaConsumer initialized (Group: {}) but no topics to monitor.",
                        processingElement.getClass().getSimpleName(), instanceID, this.heartbeatConsumerGroupId);
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
            LogUtil.error(e, "[HB] {} Processing Element {}: Failed to ensure own publish topics exist", processingElement.getClass().getSimpleName(), instanceID);
        }
    }
    
    public synchronized void start() {
        if (instanceID == null) { return; }
        if (isRunning || scheduler == null) { if(scheduler == null) isRunning = true; return; }
        LogUtil.info("[HB MANAGER] {} Processing Element {}: Starting...", processingElement.getClass().getSimpleName(), instanceID);
        isRunning = true;

        initializeRawKafkaClients();
        ensurePublishTopicsExist();

        // Schedule grace period completion
        if (scheduler != null && (!upstreamTopicsToMonitor.isEmpty() || !downstreamTopicsToMonitor.isEmpty())) {
            scheduler.schedule(() -> {
                LogUtil.info("[HB MANAGER] {} ProcessingElement {}: Initial verification grace period ended." + "Grace Time: "+INITIAL_VERIFICATION_GRACE_PERIOD_MS, processingElement.getClass().getSimpleName(), instanceID);
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
                         LogUtil.info("[HB SEND] {} Processing Element {}: Sent heartbeat to UPSTREAM topic {}", processingElement.getClass().getSimpleName(), instanceID, upstreamHeartbeatPublishTopic);
                    }
                    if (downstreamHeartbeatPublishTopic != null) {
                        heartbeatProducer.send(new ProducerRecord<>(downstreamHeartbeatPublishTopic, serializedHeartbeat));
                        LogUtil.info("[HB SEND] {} Processing Element {}: Sent heartbeat to DOWNSTREAM topic {}", processingElement.getClass().getSimpleName(), instanceID, downstreamHeartbeatPublishTopic);                    
                    }
                } catch (Exception e) { LogUtil.error(e, "[HB MANAGER] Error sending heartbeat for {}", instanceID);}
            }, ThreadLocalRandom.current().nextInt(200, 700), HEARTBEAT_SEND_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }

        // Poll for neighbor heartbeats & Check Liveness Loop
        List<String> allTopicsToSubscribe = Stream.concat(upstreamTopicsToMonitor.stream(), downstreamTopicsToMonitor.stream())
                                                 .distinct().collect(Collectors.toList());

        if (heartbeatConsumer != null && !allTopicsToSubscribe.isEmpty()) {
             heartbeatConsumer.subscribe(allTopicsToSubscribe); // Ensure subscription is set if re-starting
            scheduler.scheduleWithFixedDelay(() -> {
                if (!isRunning || !processingElement.isAvailable()) return; // Also check owner availability before reacting
                try {
                    // 1. Poll for incoming heartbeats
                    ConsumerRecords<String, String> records = heartbeatConsumer.poll(Duration.ofMillis(1000)); // Short poll 1sec, check runs often
                    Instant now = Instant.now();
                    records.forEach(record -> {
                        Message deserialized = MessageFactory.deserialize(record.value());
                        if (deserialized instanceof Heartbeat heartbeat) {
                            LogUtil.info("[HB RECV] {} with id {} received heartbeat from {} on topic {}", processingElement.getClass().getSimpleName(), instanceID, heartbeat.getInstanceID(), record.topic());
                            lastHeartbeatOnTopic.put(record.topic(), heartbeat.getTimestamp()); // Use message timestamp
                        }
                    });

                    if(!verificationGracePeriodOver) { return;}

                    // 2. Verify Upstream Peers (based on their topics)
                    if (!upstreamTopicsToMonitor.isEmpty()) {
                        if (!upstreamStrategy.verifyLiveness(new HashMap<>(lastHeartbeatOnTopic), now, HEARTBEAT_TIMEOUT_MS, upstreamTopicsToMonitor)) {
                            LogUtil.info("[HB MANAGER FAULT] {} Processing Element {}: Upstream liveness check FAILED.", processingElement.getClass().getSimpleName(), instanceID);
                            Set<String> silentUpstreamTopics = upstreamTopicsToMonitor.stream()
                                .filter(topic -> !upstreamStrategy.isTopicTimely(lastHeartbeatOnTopic.getOrDefault(topic, Instant.MIN), now, HEARTBEAT_TIMEOUT_MS))
                                .collect(Collectors.toSet());
                            reactionHandler.processLivenessFailure(new FaultContext(PeerDirection.UPSTREAM_PRODUCER, silentUpstreamTopics, upstreamTopicsToMonitor));
                        }
                    }

                    // 3. Verify Downstream Peers (based on their topics)
                    if (!downstreamTopicsToMonitor.isEmpty()) {
                        if (!downstreamStrategy.verifyLiveness(new HashMap<>(lastHeartbeatOnTopic), now, HEARTBEAT_TIMEOUT_MS, downstreamTopicsToMonitor)) {
                            LogUtil.info("[HB MANAGER FAULT] {} Processing Element {}: Downstream liveness check FAILED.", processingElement.getClass().getSimpleName(), instanceID);
                             Set<String> silentDownstreamTopics = downstreamTopicsToMonitor.stream()
                                .filter(topic -> !downstreamStrategy.isTopicTimely(lastHeartbeatOnTopic.getOrDefault(topic, Instant.MIN), now, HEARTBEAT_TIMEOUT_MS))
                                .collect(Collectors.toSet());
                            reactionHandler.processLivenessFailure(new FaultContext(PeerDirection.DOWNSTREAM_CONSUMER, silentDownstreamTopics, downstreamTopicsToMonitor));
                        }
                    }
                } catch (Exception e) { if (isRunning) LogUtil.error(e, "[HB MANAGER] Error in poll/check loop for {}", instanceID);}
            }, LIVENESS_CHECK_INTERVAL_MS, LIVENESS_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
        LogUtil.info("[HB MANAGER] {} Processing Element {}: Started successfully.", processingElement.getClass().getSimpleName(), instanceID);
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

        LogUtil.info("[HB MANAGER] {} Processing Element {}: Attempting to delete own publish topics: {}", processingElement.getClass().getSimpleName(), instanceID, topicsToDelete);
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
                LogUtil.info("[HB MANAGER] {} Processing Element {}: Successfully deleted own heartbeat publish topics: {}",
                             processingElement.getClass().getSimpleName(), instanceID, topicsThatActuallyExist);
            } else {
                LogUtil.info("[HB MANAGER] {} Processing Element {}: Own publish topics {} did not exist or were already deleted.",
                             processingElement.getClass().getSimpleName(), instanceID, topicsToDelete);
            }        
        } catch (Exception e) { // Catch any other Kafka or admin client related exceptions
            LogUtil.error(e, "[HB MANAGER ERR] {} Processing Element {}: Unexpected error deleting topics {}", processingElement.getClass().getSimpleName(), instanceID, topicsToDelete);
        }
    }

    private void deleteOwnConsumerGroup() {
        if (heartbeatConsumerGroupId == null || heartbeatConsumerGroupId.isEmpty()) {
            LogUtil.info("[HB MANAGER] {} Processing Element {}: No consumer group ID stored to delete.", processingElement.getClass().getSimpleName(), instanceID);
            return;
        }

        LogUtil.info("[HB MANAGER] {} Processing Element {}: Attempting to delete own consumer group: {}", processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            DeleteConsumerGroupsResult deleteResult = adminClient.deleteConsumerGroups(Collections.singletonList(heartbeatConsumerGroupId));
            deleteResult.all().get(KAFKA_ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            LogUtil.info("[HB MANAGER] {} Processing Element {}: Successfully deleted own consumer group: {}",
                         processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
        } catch (TimeoutException e) {
            LogUtil.error(e, "[HB MANAGER] {} Processing Element {}: Timeout deleting consumer group {}", processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
        } catch (InterruptedException e) {
            LogUtil.error(e, "[HB MANAGER] {} Processing Element {}: Interrupted deleting consumer group {}", processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof GroupIdNotFoundException) {
                LogUtil.info("[HB MANAGER] {} Processing Element {}: Consumer group {} not found (possibly already deleted or never fully formed).", processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
            } else if (e.getCause() instanceof GroupNotEmptyException) {
                 LogUtil.info("[HB MANAGER] {} Processing Element {}: Consumer group {} is not empty. Cannot delete. Ensure consumer was fully closed and timed out.", processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
            } else {
                LogUtil.error(e, "[HB MANAGER] {} Processing Element {}: ExecutionException deleting consumer group {}", processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
            }
        } catch (Exception e) {
             LogUtil.error(e, "[HB MANAGER] {} Processing Element {}: Unexpected error deleting consumer group {}", processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
        }
    }

    public synchronized void stop() {
        if (!isRunning) {
            return;
        }
        LogUtil.info("[HB MANAGER] {} Processing Element {}: Stopping...", processingElement.getClass().getSimpleName(), instanceID);
        isRunning = false;

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();                    
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

    Thread cleanupThread = new Thread(() -> {
        LogUtil.info("[HB MGR V2 CLEANUP THREAD] {} Owner {}: Starting Kafka resource cleanup.", processingElement.getClass().getSimpleName(), instanceID);
        // Close Producer
        if (heartbeatProducer != null) {
            try {
                heartbeatProducer.flush();
                heartbeatProducer.close(Duration.ofSeconds(KAFKA_CLIENT_CLOSE_TIMEOUT_SECONDS));
            } catch (Exception e) { LogUtil.error(e, "[HB MGR V2 CLEANUP] Error closing heartbeatProducer for {}", instanceID); }
        }

        // Close Consumer
        if (heartbeatConsumer != null) {
            try {
                heartbeatConsumer.unsubscribe();
                heartbeatConsumer.close(Duration.ofSeconds(KAFKA_CLIENT_CLOSE_TIMEOUT_SECONDS));
            } catch (Exception e) { LogUtil.error(e, "[HB MGR V2 CLEANUP] Error closing neighborHeartbeatConsumer for {}", instanceID); }
        }

        // Delete topics and consumer group
        deleteOwnPublishTopics();  
        deleteOwnConsumerGroup();
        LogUtil.info("[HB MGR V2 CLEANUP THREAD] {} Owner {}: Kafka resource cleanup finished.", processingElement.getClass().getSimpleName(), instanceID);
    });
    cleanupThread.setName("HBManagerCleanup-" + instanceID.substring(0, Math.min(8, instanceID.length())));
    cleanupThread.start();

    LogUtil.info("[HB MGR V2] {} Owner {}: Stop sequence initiated, cleanup delegated to separate thread.", processingElement.getClass().getSimpleName(), instanceID);
}
}
