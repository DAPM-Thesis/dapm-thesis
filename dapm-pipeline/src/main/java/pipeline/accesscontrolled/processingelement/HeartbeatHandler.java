package pipeline.accesscontrolled.processingelement;

import communication.Consumer;
import communication.Producer;
import communication.message.impl.Heartbeat;
import communication.message.impl.Heartbeat.TokenStatus;
import communication.message.serialization.deserialization.DeserializationStrategy;
import communication.message.serialization.deserialization.HeartbeatDeserializationStrategy;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HeartbeatHandler {
    private final AccessControlledProcessingElement<?> acpe;
    private final DeserializationStrategy deserializationStrategy = new HeartbeatDeserializationStrategy();
    private Producer heartbeatUpProducer;
    private final ConcurrentMap<String, Instant> lastSeenDownstreamHeartbeat = new ConcurrentHashMap<>();
    public HeartbeatHandler(AccessControlledProcessingElement<?> acpe) {
        this.acpe = acpe;
    }
    public void registerHeartbeatProducer(Producer producer){
        this.heartbeatUpProducer = producer;
    }
    public void registerHeartbeatConsumer(Consumer consumer){
        consumer.start();
    }
    public void sendHeartbeat(){
        if(heartbeatUpProducer == null) return;

        Heartbeat heartbeat = new Heartbeat(
                deserializationStrategy,
                acpe.getProcessingElement().toString(),
                Instant.now(),
                acpe.getToken().isValid()
                    ? TokenStatus.VALID
                        : TokenStatus.REVOKED,
                false
        );

        heartbeatUpProducer.publish(heartbeat);
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
    public ConcurrentMap<String, Instant> getLastSeenDownstreamHeartbeat(){
        return lastSeenDownstreamHeartbeat;
    }
}