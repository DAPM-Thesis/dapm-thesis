package pipeline.processingelement.heartbeat;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Strategy decides, for a given direction (upstream/downstream) and policy (ALL vs ANY),
 * which topics are late at THIS check.
 */
public interface HeartbeatVerificationStrategy {

    /**
     * Return true/false for single-shot liveness.
     */
    default boolean verifyLiveness(Map<String, Instant> lastHeartbeatOnMonitoredTopics,
                                   Instant currentTime,
                                   long timeoutMillis,
                                   Set<String> expectedTopicsInGroupForThisDirection) {
        return topicsNotTimely(lastHeartbeatOnMonitoredTopics, currentTime, timeoutMillis, expectedTopicsInGroupForThisDirection).isEmpty();
    }

    /**
     * Identify which expected topics are currently late (i.e., exceeded timeout).
     * The manager will interpret these with miss counters & thresholds.
     */
    default Set<String> topicsNotTimely(Map<String, Instant> lastHeartbeatOnMonitoredTopics,
                                        Instant currentTime,
                                        long timeoutMillis,
                                        Set<String> expectedTopicsInGroupForThisDirection) {
        if (expectedTopicsInGroupForThisDirection == null || expectedTopicsInGroupForThisDirection.isEmpty()) {
            return Set.of();
        }
        return expectedTopicsInGroupForThisDirection.stream()
                .filter(t -> !isTopicTimely(lastHeartbeatOnMonitoredTopics.get(t), currentTime, timeoutMillis))
                .collect(Collectors.toSet());
    }

    default boolean isTopicTimely(Instant heartbeatTime, Instant currentTime, long timeoutMillis) {
        if (heartbeatTime == null || Instant.MIN.equals(heartbeatTime)) return false;
        return java.time.Duration.between(heartbeatTime, currentTime).toMillis() <= timeoutMillis;
    }
}
