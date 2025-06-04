package pipeline.processingelement.heartbeat;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import utils.LogUtil;

public class AllDownstreamTopicsActiveStrategy implements HeartbeatVerificationStrategy {
    @Override
    public boolean verifyLiveness(Map<String, Instant> lastHeartbeatOnMonitoredTopics,
                                  Instant currentTime,
                                  long timeoutMillis,
                                  Set<String> expectedTopicsInGroupForThisDirection) {
        if (expectedTopicsInGroupForThisDirection == null || expectedTopicsInGroupForThisDirection.isEmpty()) {
            return true;
        }
        for (String expectedTopic : expectedTopicsInGroupForThisDirection) {
            Instant lastHeartbeat = lastHeartbeatOnMonitoredTopics.get(expectedTopic);
            if (!isTopicTimely(lastHeartbeat, currentTime, timeoutMillis)) {
                LogUtil.debug("[HB STRATEGY AllDownstream] Monitored downstream topic {} is not timely (last heartbeat: {})", expectedTopic, lastHeartbeat);
                return false; // One expected downstream topic is missing or not timely
            }
        }
        return true; // All expected downstream topics are timely
    }
}