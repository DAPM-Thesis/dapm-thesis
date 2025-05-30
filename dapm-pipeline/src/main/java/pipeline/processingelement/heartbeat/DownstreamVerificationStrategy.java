package pipeline.processingelement.heartbeat;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import utils.LogUtil;

public class DownstreamVerificationStrategy implements HeartbeatVerificationStrategy {
    @Override
    public boolean verifyLiveness(Map<String, Instant> lastHeartbeatOnMonitoredTopics,
                                  Instant currentTime,
                                  long timeoutMillis,
                                  Set<String> expectedTopicsInGroupForThisDirection) {
        if (expectedTopicsInGroupForThisDirection == null || expectedTopicsInGroupForThisDirection.isEmpty()) {
            return true; // No downstream consumers configured/expected to monitor for this PE.
        }
        for (String expectedTopic : expectedTopicsInGroupForThisDirection) {
            Instant lastHeartbeat = lastHeartbeatOnMonitoredTopics.get(expectedTopic);
            if (isTopicTimely(lastHeartbeat, currentTime, timeoutMillis)) {
                return true;
            }
        }
        LogUtil.debug("[HB STRATEGY Downstream] No downstream topics are timely among: {}", expectedTopicsInGroupForThisDirection);
        return false;
    }
}