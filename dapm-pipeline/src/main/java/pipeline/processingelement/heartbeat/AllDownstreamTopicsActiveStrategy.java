package pipeline.processingelement.heartbeat;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * ALL downstream must be timely -> late set = any downstream topic currently late.
 */
public class AllDownstreamTopicsActiveStrategy implements HeartbeatVerificationStrategy {
    @Override
    public Set<String> topicsNotTimely(Map<String, Instant> lastHeartbeatOnMonitoredTopics,
                                       Instant currentTime,
                                       long timeoutMillis,
                                       Set<String> expectedTopicsInGroupForThisDirection) {
        return HeartbeatVerificationStrategy.super.topicsNotTimely(
                lastHeartbeatOnMonitoredTopics, currentTime, timeoutMillis, expectedTopicsInGroupForThisDirection);
    }
}
