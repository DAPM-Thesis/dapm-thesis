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
import java.util.concurrent.atomic.AtomicInteger;
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
    private Set<String> upstreamTopicsToMonitor; // mutable
    private final Set<String> downstreamTopicsToMonitor;
    private final Set<String> optionalUpstreamTopics;
    private String heartbeatConsumerGroupId;

    // Last seen heartbeat per topic
    private final ConcurrentMap<String, Instant> lastHeartbeatOnTopic = new ConcurrentHashMap<>();

    // Strategies
    private final HeartbeatVerificationStrategy upstreamStrategy;
    private final HeartbeatVerificationStrategy downstreamStrategy;

    private final ScheduledExecutorService scheduler;
    private volatile boolean isRunning = false;

    // ---- THRESHOLDS AND INTERVALS ----
    private final long HEARTBEAT_SEND_INTERVAL_MS;
    private final long LIVENESS_CHECK_INTERVAL_MS; // How often to check for liveness
    private final long HEARTBEAT_TIMEOUT_MS;      // Message considered late after this
    private static final long KAFKA_ADMIN_TIMEOUT_SECONDS = 10;
    private static final long KAFKA_CLIENT_CLOSE_TIMEOUT_SECONDS = 5;
    private final int MISSES_THRESHOLD;        // <— 3 misses before declaring failure

    private volatile boolean verificationGracePeriodOver = false;
    private final long INITIAL_VERIFICATION_GRACE_PERIOD_MS;

    // ---- Miss counters & silencing ----
    // Per-topic consecutive misses
    private final ConcurrentMap<String, Integer> consecutiveMisses = new ConcurrentHashMap<>();
    // Topics already declared dead (silenced until recovery HB arrives)
    private final Set<String> silencedDeadTopics = ConcurrentHashMap.newKeySet();
    // ANY-policy aggregate state: “all downstream silent” consecutive misses
    private final AtomicInteger downstreamAllSilentConsecMisses = new AtomicInteger(0);
    private volatile boolean downstreamAllSilentSilenced = false;

    public HeartbeatManager_V2(
            ProcessingElement processingElement,
            String brokerUrl,
            HeartbeatTopicConfig topicConfig,
            ReactionHandler reactionHandler,
            HeartbeatTimingConfig timingConfig
    ) {
        this.processingElement = Objects.requireNonNull(processingElement);
        this.instanceID = Objects.requireNonNull(processingElement.getInstanceId());
        this.brokerUrl = Objects.requireNonNull(brokerUrl);
        Objects.requireNonNull(topicConfig, "HeartbeatTopicSetupConfig cannot be null");

        // (Keep timingConfig hooks commented; using constants for now)
        this.HEARTBEAT_SEND_INTERVAL_MS = timingConfig.getSendIntervalMs();
        this.LIVENESS_CHECK_INTERVAL_MS = timingConfig.getCheckIntervalMs();
        this.HEARTBEAT_TIMEOUT_MS       = timingConfig.getTimeoutMs();
        this.INITIAL_VERIFICATION_GRACE_PERIOD_MS = timingConfig.getInitialGracePeriodMs();
        this.MISSES_THRESHOLD = timingConfig.getMissesThreshold();

        this.reactionHandler = Objects.requireNonNull(reactionHandler, "ReactionHandler cannot be null");

        this.upstreamStrategy = new UpstreamVerificationStrategy();
        if (processingElement.getFaultToleranceLevel() == FaultToleranceLevel.LEVEL_TERMINATE_ENTIRE_PIPELINE
                || processingElement.getFaultToleranceLevel() == FaultToleranceLevel.LEVEL_NOTIFY_ONLY) {
            this.downstreamStrategy = new AllDownstreamTopicsActiveStrategy();
        } else {
            this.downstreamStrategy = new AnyDownstreamVerificationStrategy();
        }

        this.upstreamHeartbeatPublishTopic = topicConfig.getUpstreamHeartbeatPublishTopic();
        this.downstreamHeartbeatPublishTopic = topicConfig.getDownstreamHeartbeatPublishTopic();

        this.upstreamTopicsToMonitor = new HashSet<>(topicConfig.getUpstreamNeighborHeartbeatTopicsToSubscribeTo());
        this.downstreamTopicsToMonitor = Collections.unmodifiableSet(new HashSet<>(topicConfig.getDownstreamNeighborHeartbeatTopicsToSubscribeTo()));
        this.optionalUpstreamTopics = Collections.unmodifiableSet(new HashSet<>(topicConfig.getOptionalUpstreamNeighborTopics()));

        Stream.concat(upstreamTopicsToMonitor.stream(), downstreamTopicsToMonitor.stream())
                .forEach(topic -> {
                    lastHeartbeatOnTopic.put(topic, Instant.MIN);
                    consecutiveMisses.put(topic, 0);
                });

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
            LogUtil.info("[HB MANAGER] {} Processing Element {}: Raw KafkaProducer initialized.",
                    processingElement.getClass().getSimpleName(), instanceID);
        }

        if (needsConsumer && heartbeatConsumer == null) {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
            this.heartbeatConsumerGroupId = "hb-consumer-" + instanceID + "-" + UUID.randomUUID().toString().substring(0, 8);
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
                LogUtil.info("[HB TOPIC] {} ProcessingElement {}: Created own publish topics: {}",
                        processingElement.getClass().getSimpleName(), instanceID,
                        newTopics.stream().map(NewTopic::name).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            LogUtil.error(e, "[HB] {} Processing Element {}: Failed to ensure own publish topics exist",
                    processingElement.getClass().getSimpleName(), instanceID);
        }
    }

    public synchronized void start() {
        if (instanceID == null) return;
        if (isRunning || scheduler == null) { if (scheduler == null) isRunning = true; return; }

        LogUtil.info("[HB MANAGER] {} Processing Element {}: Starting...",
                processingElement.getClass().getSimpleName(), instanceID);
        isRunning = true;

        initializeRawKafkaClients();
        ensurePublishTopicsExist();

        // Schedule grace period completion
        if (scheduler != null && (!upstreamTopicsToMonitor.isEmpty() || !downstreamTopicsToMonitor.isEmpty())) {
            scheduler.schedule(() -> {
                LogUtil.info("[HB MANAGER] {} ProcessingElement {}: Initial verification grace period ended. Grace Time: {}ms",
                        processingElement.getClass().getSimpleName(), instanceID, INITIAL_VERIFICATION_GRACE_PERIOD_MS);
                verificationGracePeriodOver = true;
            }, INITIAL_VERIFICATION_GRACE_PERIOD_MS, TimeUnit.MILLISECONDS);
        } else if (scheduler == null) {
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
                        LogUtil.info("[HB SEND] {} {}: Sent heartbeat to UPSTREAM topic {}",
                                processingElement.getClass().getSimpleName(), instanceID, upstreamHeartbeatPublishTopic);
                    }
                    if (downstreamHeartbeatPublishTopic != null) {
                        heartbeatProducer.send(new ProducerRecord<>(downstreamHeartbeatPublishTopic, serializedHeartbeat));
                        LogUtil.info("[HB SEND] {} {}: Sent heartbeat to DOWNSTREAM topic {}",
                                processingElement.getClass().getSimpleName(), instanceID, downstreamHeartbeatPublishTopic);
                    }
                } catch (Exception e) {
                    LogUtil.error(e, "[HB MANAGER] Error sending heartbeat for {}", instanceID);
                }
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
                    // 1) Poll for incoming heartbeats
                    ConsumerRecords<String, String> records = heartbeatConsumer.poll(Duration.ofMillis(1000));
                    Instant now = Instant.now();

                    records.forEach(record -> {
                        Message deserialized = MessageFactory.deserialize(record.value());
                        if (deserialized instanceof Heartbeat hb) {
                            String topic = record.topic();
                            Instant prevTs = lastHeartbeatOnTopic.getOrDefault(topic, Instant.MIN);
                            // Update last-seen timestamp from payload timestamp
                            lastHeartbeatOnTopic.put(topic, hb.getTimestamp());

                            // Recovery handling
                            Integer prevMiss = consecutiveMisses.getOrDefault(topic, 0);
                            boolean wasSilenced = silencedDeadTopics.remove(topic);
                            if (prevMiss != null && prevMiss > 0) {
                                LogUtil.info("[HB RECOVERY] {} {}: topic {} recovered; consecutive misses reset from {}.",
                                        processingElement.getClass().getSimpleName(), instanceID, topic, prevMiss);
                            }
                            if (wasSilenced) {
                                long downForMs = prevTs.equals(Instant.MIN) ? -1
                                        : java.time.Duration.between(prevTs, hb.getTimestamp()).toMillis();
                                LogUtil.info("[HB RECOVERY] {} {}: silenced topic {} is alive again (downtime={}ms). Re-enabling checks.",
                                        processingElement.getClass().getSimpleName(), instanceID, topic, downForMs);
                            }

                            consecutiveMisses.put(topic, 0);
                            LogUtil.info("[HB RECV] {} {} received heartbeat from {} on topic {}",
                                    processingElement.getClass().getSimpleName(), instanceID, hb.getInstanceID(), topic);
                        }
                    });

                    if (!verificationGracePeriodOver) return;

                    // 2) Upstream liveness (per-topic counters + silencing)
                    upstreamLivenessCheck();

                    // 3) Downstream liveness (ANY vs ALL policies)
                    if (!downstreamTopicsToMonitor.isEmpty()) {
                        checkDownstreamLiveness(now);
                    }

                } catch (Exception e) {
                    if (isRunning) LogUtil.error(e, "[HB MANAGER] Error in poll/check loop for {}", instanceID);
                }
            }, LIVENESS_CHECK_INTERVAL_MS, LIVENESS_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }

        LogUtil.info("[HB MANAGER] {} Processing Element {}: Started successfully.",
                processingElement.getClass().getSimpleName(), instanceID);
    }

    private void upstreamLivenessCheck() {
        if (!isRunning || !verificationGracePeriodOver || upstreamTopicsToMonitor.isEmpty()) return;

        Instant now = Instant.now();

        // Evaluate each upstream topic individually
        for (String topic : new HashSet<>(upstreamTopicsToMonitor)) {
            if (silencedDeadTopics.contains(topic)) {
                // already declared; wait for recovery HB to unsilence
                continue;
            }

            Instant last = lastHeartbeatOnTopic.getOrDefault(topic, Instant.MIN);
            boolean timely = upstreamStrategy.isTopicTimely(last, now, HEARTBEAT_TIMEOUT_MS);

            if (timely) {
                int prev = consecutiveMisses.getOrDefault(topic, 0);
                if (prev > 0) {
                    LogUtil.info("[HB RESET] {} {}: topic {} back to timely; reset consecutive misses from {} to 0.",
                            processingElement.getClass().getSimpleName(), instanceID, topic, prev);
                }
                consecutiveMisses.put(topic, 0);
                continue;
            }

            // late → increment counter
            int miss = consecutiveMisses.merge(topic, 1, Integer::sum);

            if (miss < MISSES_THRESHOLD) {
                LogUtil.info("[HB UPSTREAM] {} {}: timeout miss {}/{} on topic {} (lastSeen={})",
                        processingElement.getClass().getSimpleName(), instanceID,
                        miss, MISSES_THRESHOLD, topic, last);
            } else if (miss == MISSES_THRESHOLD) {
                boolean isCritical = !optionalUpstreamTopics.contains(topic);
                LogUtil.info("[HB UPSTREAM] {} {}: {} upstream topic exceeded threshold ({} misses): [{}] — declaring failure.",
                        processingElement.getClass().getSimpleName(), instanceID,
                        (isCritical ? "CRITICAL" : "OPTIONAL"), MISSES_THRESHOLD, topic);

                // Silence this topic going forward to avoid 4/3, 5/3, ... spam
                silencedDeadTopics.add(topic);

                FaultContext fc = new FaultContext(PeerDirection.UPSTREAM_PRODUCER,
                        Collections.singleton(topic), upstreamTopicsToMonitor);
                reactionHandler.processLivenessFailure(fc, !isCritical);

                if (!isCritical) {
                    // optional → stop monitoring after silencing
                    stopMonitoringTopics(Collections.singleton(topic));
                }
            } else {
                // miss > threshold → keep quiet; optionally log rare debug
                if ((miss - MISSES_THRESHOLD) % 12 == 0) {
                    long ageMs = last.equals(Instant.MIN) ? -1 : java.time.Duration.between(last, now).toMillis();
                    LogUtil.debug("[HB UPSTREAM] {} {}: topic {} still late ({} misses, age={}ms) — silenced.",
                            processingElement.getClass().getSimpleName(), instanceID, topic, miss, ageMs);
                }
            }
        }
    }

    private void checkDownstreamLiveness(Instant now) {
        // Compute which downstream topics are late
        Set<String> late = downstreamTopicsToMonitor.stream()
                .filter(t -> !downstreamStrategy.isTopicTimely(
                        lastHeartbeatOnTopic.getOrDefault(t, Instant.MIN), now, HEARTBEAT_TIMEOUT_MS))
                .collect(Collectors.toSet());

        if (downstreamStrategy instanceof AllDownstreamTopicsActiveStrategy) {
            // ALL policy → every topic must be timely; handle per-topic counters like upstream
            if (!late.isEmpty()) {
                for (String t : late) {
                    if (silencedDeadTopics.contains(t)) continue; // declared already
                    int miss = consecutiveMisses.merge(t, 1, Integer::sum);
                    if (miss < MISSES_THRESHOLD) {
                        LogUtil.info("[HB DOWNSTREAM] {} {}: timeout miss {}/{} on topic {} (lastSeen={})",
                                processingElement.getClass().getSimpleName(), instanceID,
                                miss, MISSES_THRESHOLD, t, lastHeartbeatOnTopic.getOrDefault(t, Instant.MIN));
                    } else if (miss == MISSES_THRESHOLD) {
                        silencedDeadTopics.add(t);
                        LogUtil.info("[HB MANAGER FAULT] {} {}: Downstream liveness FAILED (ALL policy). Silent topic: {}.",
                                processingElement.getClass().getSimpleName(), instanceID, t);
                        reactionHandler.processLivenessFailure(
                                new FaultContext(PeerDirection.DOWNSTREAM_CONSUMER, Collections.singleton(t), downstreamTopicsToMonitor), false);
                    }
                }
            } else {
                // Every downstream timely → reset counters & unsilence if needed
                for (String t : downstreamTopicsToMonitor) {
                    int prev = consecutiveMisses.getOrDefault(t, 0);
                    boolean wasSilenced = silencedDeadTopics.remove(t);
                    if (prev > 0 || wasSilenced) {
                        LogUtil.info("[HB RESET] {} {}: downstream topic {} timely; reset consecutive misses from {}.",
                                processingElement.getClass().getSimpleName(), instanceID, t, prev);
                    }
                    consecutiveMisses.put(t, 0);
                }
            }
        } else {
            // ANY policy → declare only if *all* downstream are late, tracked as an aggregate counter
            boolean allSilent = !downstreamTopicsToMonitor.isEmpty() && late.containsAll(downstreamTopicsToMonitor);
            if (allSilent) {
                if (!downstreamAllSilentSilenced) {
                    int miss = downstreamAllSilentConsecMisses.incrementAndGet();
                    if (miss < MISSES_THRESHOLD) {
                        LogUtil.info("[HB DOWNSTREAM] {} {}: ANY-policy all-silent miss {}/{} (all {} downstream topics are late).",
                                processingElement.getClass().getSimpleName(), instanceID,
                                miss, MISSES_THRESHOLD, downstreamTopicsToMonitor.size());
                    } else if (miss == MISSES_THRESHOLD) {
                        downstreamAllSilentSilenced = true;
                        LogUtil.info("[HB MANAGER FAULT] {} {}: Downstream liveness FAILED (ANY policy) — all downstream topics silent for {} consecutive checks.",
                                processingElement.getClass().getSimpleName(), instanceID, MISSES_THRESHOLD);
                        reactionHandler.processLivenessFailure(
                                new FaultContext(PeerDirection.DOWNSTREAM_CONSUMER, new HashSet<>(late), downstreamTopicsToMonitor), false);
                    }
                }
            } else {
                // Recovery of the aggregate condition
                int prev = downstreamAllSilentConsecMisses.getAndSet(0);
                if (prev > 0 || downstreamAllSilentSilenced) {
                    LogUtil.info("[HB DOWNSTREAM] {} {}: ANY-policy recovered (some downstream timely again). Reset aggregate misses from {}.",
                            processingElement.getClass().getSimpleName(), instanceID, prev);
                }
                downstreamAllSilentSilenced = false;
            }
        }
    }

    private synchronized void stopMonitoringTopics(Set<String> topicsToUnsubscribe) {
        if (topicsToUnsubscribe == null || topicsToUnsubscribe.isEmpty()) return;

        boolean changed = this.upstreamTopicsToMonitor.removeAll(topicsToUnsubscribe);

        if (changed) {
            // Clean up counters/state for removed topics
            topicsToUnsubscribe.forEach(t -> {
                consecutiveMisses.remove(t);
                silencedDeadTopics.remove(t);
                lastHeartbeatOnTopic.remove(t);
            });

            LogUtil.info("[HB MANAGER] Unsubscribing from failed optional topics: {}", topicsToUnsubscribe);

            Set<String> allTopicsToStillMonitor = new HashSet<>(this.upstreamTopicsToMonitor);
            allTopicsToStillMonitor.addAll(this.downstreamTopicsToMonitor);

            if (this.heartbeatConsumer != null) {
                if (allTopicsToStillMonitor.isEmpty()) {
                    this.heartbeatConsumer.unsubscribe();
                } else {
                    // Re-subscribe to the new, smaller set of topics
                    this.heartbeatConsumer.subscribe(new ArrayList<>(allTopicsToStillMonitor));
                }
            }
        }
    }

    private void deleteOwnPublishTopics() {
        List<String> topicsToDelete = new ArrayList<>();
        if (upstreamHeartbeatPublishTopic != null && !upstreamHeartbeatPublishTopic.isEmpty()) {
            topicsToDelete.add(upstreamHeartbeatPublishTopic);
        }
        if (downstreamHeartbeatPublishTopic != null && !downstreamHeartbeatPublishTopic.isEmpty()) {
            topicsToDelete.add(downstreamHeartbeatPublishTopic);
        }
        if (topicsToDelete.isEmpty()) return;

        LogUtil.info("[HB MANAGER] {} Processing Element {}: Attempting to delete own publish topics: {}",
                processingElement.getClass().getSimpleName(), instanceID, topicsToDelete);
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            Set<String> existingTopics = adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
            List<String> topicsThatActuallyExist = topicsToDelete.stream()
                    .filter(existingTopics::contains)
                    .collect(Collectors.toList());

            if (!topicsThatActuallyExist.isEmpty()) {
                DeleteTopicsResult deleteResult = adminClient.deleteTopics(topicsThatActuallyExist);
                deleteResult.all().get(10, TimeUnit.SECONDS);
                LogUtil.info("[HB MANAGER] {} Processing Element {}: Successfully deleted own heartbeat publish topics: {}",
                        processingElement.getClass().getSimpleName(), instanceID, topicsThatActuallyExist);
            } else {
                LogUtil.info("[HB MANAGER] {} Processing Element {}: Own publish topics {} did not exist or were already deleted.",
                        processingElement.getClass().getSimpleName(), instanceID, topicsToDelete);
            }
        } catch (Exception e) {
            LogUtil.error(e, "[HB MANAGER ERR] {} Processing Element {}: Unexpected error deleting topics {}",
                    processingElement.getClass().getSimpleName(), instanceID, topicsToDelete);
        }
    }

    private void deleteOwnConsumerGroup() {
        if (heartbeatConsumerGroupId == null || heartbeatConsumerGroupId.isEmpty()) {
            LogUtil.info("[HB MANAGER] {} Processing Element {}: No consumer group ID stored to delete.",
                    processingElement.getClass().getSimpleName(), instanceID);
            return;
        }

        LogUtil.info("[HB MANAGER] {} Processing Element {}: Attempting to delete own consumer group: {}",
                processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            DeleteConsumerGroupsResult deleteResult = adminClient.deleteConsumerGroups(Collections.singletonList(heartbeatConsumerGroupId));
            deleteResult.all().get(KAFKA_ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            LogUtil.info("[HB MANAGER] {} Processing Element {}: Successfully deleted own consumer group: {}",
                    processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
        } catch (TimeoutException e) {
            LogUtil.error(e, "[HB MANAGER] {} Processing Element {}: Timeout deleting consumer group {}",
                    processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
        } catch (InterruptedException e) {
            LogUtil.error(e, "[HB MANAGER] {} Processing Element {}: Interrupted deleting consumer group {}",
                    processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof GroupIdNotFoundException) {
                LogUtil.info("[HB MANAGER] {} Processing Element {}: Consumer group {} not found (already deleted or never formed).",
                        processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
            } else if (e.getCause() instanceof GroupNotEmptyException) {
                LogUtil.info("[HB MANAGER] {} Processing Element {}: Consumer group {} is not empty. Cannot delete.",
                        processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
            } else {
                LogUtil.error(e, "[HB MANAGER] {} Processing Element {}: ExecutionException deleting consumer group {}",
                        processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
            }
        } catch (Exception e) {
            LogUtil.error(e, "[HB MANAGER] {} Processing Element {}: Unexpected error deleting consumer group {}",
                    processingElement.getClass().getSimpleName(), instanceID, heartbeatConsumerGroupId);
        }
    }

    public synchronized void stop() {
        if (!isRunning) return;

        LogUtil.info("[HB MANAGER] {} Processing Element {}: Stopping...",
                processingElement.getClass().getSimpleName(), instanceID);
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
            LogUtil.info("[HB MGR V2 CLEANUP THREAD] {} Owner {}: Starting Kafka resource cleanup.",
                    processingElement.getClass().getSimpleName(), instanceID);

            // Close Producer
            if (heartbeatProducer != null) {
                try {
                    heartbeatProducer.flush();
                    heartbeatProducer.close(Duration.ofSeconds(KAFKA_CLIENT_CLOSE_TIMEOUT_SECONDS));
                } catch (Exception e) {
                    LogUtil.error(e, "[HB MGR V2 CLEANUP] Error closing heartbeatProducer for {}", instanceID);
                }
            }

            // Close Consumer
            if (heartbeatConsumer != null) {
                try {
                    heartbeatConsumer.unsubscribe();
                    heartbeatConsumer.close(Duration.ofSeconds(KAFKA_CLIENT_CLOSE_TIMEOUT_SECONDS));
                } catch (Exception e) {
                    LogUtil.error(e, "[HB MGR V2 CLEANUP] Error closing neighborHeartbeatConsumer for {}", instanceID);
                }
            }

            // Delete topics and consumer group
            deleteOwnPublishTopics();
            deleteOwnConsumerGroup();
            LogUtil.info("[HB MGR V2 CLEANUP THREAD] {} Owner {}: Kafka resource cleanup finished.",
                    processingElement.getClass().getSimpleName(), instanceID);
        });

        cleanupThread.setName("HBManagerCleanup-" + instanceID.substring(0, Math.min(8, instanceID.length())));
        cleanupThread.start();

        LogUtil.info("[HB MGR V2] {} Owner {}: Stop sequence initiated, cleanup delegated to separate thread.",
                processingElement.getClass().getSimpleName(), instanceID);
    }
}
