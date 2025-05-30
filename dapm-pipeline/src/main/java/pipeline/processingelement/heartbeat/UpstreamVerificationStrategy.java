package pipeline.processingelement.heartbeat;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import utils.LogUtil;

public class UpstreamVerificationStrategy implements HeartbeatVerificationStrategy {
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
                LogUtil.debug("[HB STRATEGY Upstream] Monitored topic {} is not timely (last heartbeat: {})", expectedTopic, lastHeartbeat);
                return false;
            }
        }
        return true;
    }
}