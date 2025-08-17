package pipeline.processingelement.heartbeat;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Upstream policy is effectively "ALL required topics must be timely".
 * topicsNotTimely = every expected upstream topic that is late *right now*.
 */
public class UpstreamVerificationStrategy implements HeartbeatVerificationStrategy {
    @Override
    public Set<String> topicsNotTimely(Map<String, Instant> lastHeartbeatOnMonitoredTopics,
                                       Instant currentTime,
                                       long timeoutMillis,
                                       Set<String> expectedTopicsInGroupForThisDirection) {
        return HeartbeatVerificationStrategy.super.topicsNotTimely(
                lastHeartbeatOnMonitoredTopics, currentTime, timeoutMillis, expectedTopicsInGroupForThisDirection);
    }
}
