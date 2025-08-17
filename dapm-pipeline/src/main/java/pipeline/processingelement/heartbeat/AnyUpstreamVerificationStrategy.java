package pipeline.processingelement.heartbeat;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ANY upstream timely means the set of "late" topics is all-but-at-least-one-ok.
 * We still return the per-topic late set so the manager can count misses and log recoveries.
 */
public class AnyUpstreamVerificationStrategy implements HeartbeatVerificationStrategy {
    @Override
    public Set<String> topicsNotTimely(Map<String, Instant> lastHeartbeatOnMonitoredTopics,
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
}
