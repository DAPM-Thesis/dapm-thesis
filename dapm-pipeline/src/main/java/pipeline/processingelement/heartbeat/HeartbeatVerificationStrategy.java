package pipeline.processingelement.heartbeat;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public interface HeartbeatVerificationStrategy {
    /**
     * Verifies peer liveness based on topic activity.
     * @param lastHeartbeatOnMonitoredTopics Map of monitoredTopicName to its last received heartbeat timestamp.
     * @param currentTime Current time for comparison.
     * @param timeoutMillis Liveness timeout threshold.
     * @param expectedTopicsInGroupForThisDirection The set of all topic names that are expected for this strategy.
     * @return true if liveness criteria met, false otherwise.
     */
    boolean verifyLiveness(Map<String, Instant> lastHeartbeatOnMonitoredTopics,
                           Instant currentTime,
                           long timeoutMillis,
                           Set<String> expectedTopicsInGroupForThisDirection);

    default boolean isTopicTimely(Instant heartbeatTime, Instant currentTime, long timeoutMillis) {
        if (heartbeatTime == null || Instant.MIN.equals(heartbeatTime)) return false;
        return java.time.Duration.between(heartbeatTime, currentTime).toMillis() <= timeoutMillis;
    }
}