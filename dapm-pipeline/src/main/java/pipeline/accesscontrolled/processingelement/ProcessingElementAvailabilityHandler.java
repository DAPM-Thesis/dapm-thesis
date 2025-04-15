// TODO: Use the availability flag of the PE through ACPE??? if it brings more complexity then just the ACPE availability and remove the isAvailable from PE
package pipeline.accesscontrolled.processingelement;
public class ProcessingElementAvailabilityHandler {
    private final AccessControlledProcessingElement<?> acpe;

    public ProcessingElementAvailabilityHandler(AccessControlledProcessingElement<?> acpe) {
        this.acpe = acpe;
    }

    public void checkHeartbeatStatus() {
        // check if a heartbeat has been missed over a timeout period.
        // For now, this is a placeholder.
        // Example: if no heartbeat received in 10 seconds, then stop the PE.
    }

    public void stopProcessing() {
        acpe.setAvailable(false);
        System.out.println("Stopping processing for: " + acpe.toString());
    }

    public void resumeProcessing() {
        acpe.setAvailable(true);
        System.out.println("Resuming processing for: " + acpe.toString());
    }
}