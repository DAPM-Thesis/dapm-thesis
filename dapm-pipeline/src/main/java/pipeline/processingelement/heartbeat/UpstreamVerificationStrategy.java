package pipeline.processingelement.heartbeat;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class UpstreamVerificationStrategy implements HeartbeatVerificationStrategy {
    @Override
    public boolean verify(Map<String, Instant> latestHeartbeats, Instant currentTime, long timeoutMillis) {
        if (latestHeartbeats.isEmpty()) {
            // If there are no upstream dependencies configured, this PE doesn't depend on any upstream heartbeats.
            return true;
        }
        // All configured upstream peers must be timely.
        return latestHeartbeats.values().stream()
                .allMatch(latestReceived -> isTimely(latestReceived, currentTime, timeoutMillis));
    }

    private boolean isTimely(Instant receivedTime, Instant currentTime, long timeoutMillis) {
        // If a peer was never seen (Instant.MIN) or somehow null, it's not timely.
        if (receivedTime == null || Instant.MIN.equals(receivedTime)) {
            return false;
        }
        return Duration.between(receivedTime, currentTime).toMillis() <= timeoutMillis;
    }
}