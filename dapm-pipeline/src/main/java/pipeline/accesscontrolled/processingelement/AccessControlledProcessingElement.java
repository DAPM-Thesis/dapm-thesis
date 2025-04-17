package pipeline.accesscontrolled.processingelement;

import pipeline.processingelement.ProcessingElement;

import java.util.HashSet;
import java.util.Set;
import communication.Producer;
import communication.Consumer;
import communication.message.impl.Heartbeat;
import java.util.ArrayList;
import java.util.List;

public class AccessControlledProcessingElement<T extends ProcessingElement> extends ProcessingElement {
    private final T processingElement;
    private final ProcessingElementToken token;

    // TODO: Use the availability flag of the PE and don't introduce a new one here:  byut think first about the complexity of using isAvailable of PE
    private boolean isAvailable;
    // TODO: Use HashMap or HashSet if possible
    private final List<ChannelConfiguration> channels = new ArrayList<>();

    // Handlers for heartbeat and availability management
    private final HeartbeatHandler heartbeatHandler;
    private final ProcessingElementAvailabilityHandler availabilityHandler;

    public AccessControlledProcessingElement(T processingElement, ProcessingElementToken token) {
        super();
        this.processingElement = processingElement;
        this.token = token;
        //this.isAvailable = true; // TODO: Currenltyl initially available:: this should be set in the start method: create a method that verifies all the token and set this to available.
        this.isAvailable = ProcessingElementAccessVerifier.validateToken(token);
        this.heartbeatHandler = new HeartbeatHandler(this);
        this.availabilityHandler = new ProcessingElementAvailabilityHandler(this);
    }

    public T getProcessingElement() {
        return processingElement;
    }

    public ProcessingElementToken getToken() {
        return token;
    }
    @Override
    public boolean isAvailable() {
        return isAvailable;
    }
    @Override
    public void setAvailable(boolean available) {
        this.isAvailable = available;
        processingElement.setAvailable(available);
    }
    public void addChannelConfiguration(ChannelConfiguration config){
        channels.add(config);
    }
    // Heartbeat operations
    public List<ChannelConfiguration> getChannels() {
        return channels;
    }
    public void registerHeartbeatProducer(Producer producer){
        heartbeatHandler.registerHeartbeatProducer(producer);
    }
    public void registerHeartbeatConsumer(Consumer consumer) {
        heartbeatHandler.registerHeartbeatConsumer(consumer);
    }
    public void sendHeartbeat() {
        heartbeatHandler.sendHeartbeat();
    }
    public void receiveHeartbeat(Heartbeat hb) {
        heartbeatHandler.receiveHeartbeat(hb);
    }
    // availability
    public void checkHeartbeatStatus() {
        availabilityHandler.checkHeartbeatStatus();
    }
    public void stopProcessing() {
        availabilityHandler.stopProcessing();
    }
    public void resumeProcessing() {
        availabilityHandler.resumeProcessing();
    }
    @Override
    public String toString() {
        return "ACPE wrapping " + processingElement.toString()
                + " [Token: " + token.toString()
                + ", Available: " + isAvailable + "]";
    }
}
