package pipeline.processingelement.heartbeat;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class DownstreamVerificationStrategy implements HeartbeatVerificationStrategy {
    @Override
    public boolean verify(Map<String, Instant> latestHeartbeats, Instant currentTime, long timeoutMillis) {
        if (latestHeartbeats.isEmpty()) {
            return true;
        }
        // At least one configured downstream peer must be timely.
        return latestHeartbeats.values().stream()
                .anyMatch(latestReceived -> isTimely(latestReceived, currentTime, timeoutMillis));
    }

    private boolean isTimely(Instant receivedTime, Instant currentTime, long timeoutMillis) {
        if (receivedTime == null || Instant.MIN.equals(receivedTime)) {
            return false;
        }
        return Duration.between(receivedTime, currentTime).toMillis() <= timeoutMillis;
    }
}