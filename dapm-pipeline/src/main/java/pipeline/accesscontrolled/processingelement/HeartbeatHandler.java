package pipeline.accesscontrolled.processingelement;

import communication.message.impl.Heartbeat;
import communication.message.impl.Heartbeat.TokenStatus;
import communication.message.serialization.deserialization.DeserializationStrategy;
import communication.message.serialization.deserialization.HeartbeatDeserializationStrategy;

import java.time.Instant;

public class HeartbeatHandler {
    private final AccessControlledProcessingElement<?> acpe;
    private final DeserializationStrategy deserializationStrategy = new HeartbeatDeserializationStrategy();

    public HeartbeatHandler(AccessControlledProcessingElement<?> acpe) {
        this.acpe = acpe;
    }

    public void sendHeartbeat() {
        Heartbeat hb = new Heartbeat(deserializationStrategy,
                acpe.getProcessingElement().toString(),
                Instant.now(),
                acpe.getToken().isValid() ? TokenStatus.VALID : TokenStatus.REVOKED,
                false
        );

        if (acpe.getUpstream() != null) {
            acpe.getUpstream().receiveHeartbeat(hb);
        }
    }

    public void receiveHeartbeat(Heartbeat hb) {
        // Process the heartbeat.
        // For example, if the heartbeat indicates a revoked token, trigger stopping.
        // TODO: The immediate flag is used for only token revocation, but we can also use it for re-granting of token
        // TODO: Log the heartbeat
        if (hb.getTokenStatus() == TokenStatus.REVOKED || hb.isImmediateFlag()) {
            acpe.stopProcessing();
        }
    }
}