package pipeline.accesscontrolled.processingelement;

import pipeline.processingelement.ProcessingElement;

import java.util.HashSet;
import java.util.Set;

public class AccessControlledProcessingElement<T extends ProcessingElement> extends ProcessingElement {
    private final T processingElement;
    private final ProcessingElementToken token;

    // TODO: Use the availability flag of the PE and don't introduce a new one here:  byut think first about the complexity of using isAvailable of PE
    private boolean isAvailable;
    private AccessControlledProcessingElement<?> upstream; // null if first element
    private final Set<AccessControlledProcessingElement<?>> downstreams = new HashSet<>();

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

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
        processingElement.setAvailable(available);
    }

    public AccessControlledProcessingElement<?> getUpstream() {
        return upstream;
    }

    public void setUpstream(AccessControlledProcessingElement<?> upstream) {
        this.upstream = upstream;
    }

    public Set<AccessControlledProcessingElement<?>> getDownstreams() {
        return downstreams;
    }

    public void addDownstream(AccessControlledProcessingElement<?> downstream) {
        this.downstreams.add(downstream);
    }

    // Heartbeat operations delegated to the HeartbeatHandler
    public void sendHeartbeat() {
        heartbeatHandler.sendHeartbeat();
    }

    public void receiveHeartbeat(communication.message.impl.Heartbeat hb) {
        heartbeatHandler.receiveHeartbeat(hb);
    }

    // Local availability control via the PEAvailabilityHandler
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
