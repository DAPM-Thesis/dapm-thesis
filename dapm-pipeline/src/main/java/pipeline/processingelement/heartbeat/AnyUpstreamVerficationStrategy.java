package pipeline.processingelement.heartbeat;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import utils.LogUtil;

public class AnyUpstreamVerficationStrategy implements HeartbeatVerificationStrategy {
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
            if (isTopicTimely(lastHeartbeat, currentTime, timeoutMillis)) {  // RETURN EARLY IF ANY TOPIC IS TIMELY
                LogUtil.debug("[HB STRATEGY Upstream] Monitored topic {} is timely (last heartbeat: {})", expectedTopic, lastHeartbeat);
                return true;
            }
        }
        return false;
    }
}
