package pipeline.processingelement.heartbeat;

import java.time.Instant;
import java.util.Map;

public interface HeartbeatVerificationStrategy {
    /**
     * Verifies if the peers are considered live based on their latest heartbeat timestamps.
     *
     * @param latestHeartbeats A map of peer instance IDs to their last seen heartbeat timestamp.
     * @param currentTime The current time to compare against.
     * @param timeoutMillis The maximum allowed duration since the last heartbeat for a peer to be considered live.
     * @return true if the peers meet the liveness criteria defined by the strategy, false otherwise.
     */
    boolean verify(Map<String, Instant> latestHeartbeats, Instant currentTime, long timeoutMillis);
}