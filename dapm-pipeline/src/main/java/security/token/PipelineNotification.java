package security.token;

import java.time.Instant;

public class PipelineNotification {
    private String processingElementID;
    private boolean isAvailable;
    private String description;
    private Instant time;
    public PipelineNotification(String processingElementID, boolean isAvailable, String description, Instant time) {
        this.processingElementID = processingElementID;
        this.isAvailable = isAvailable;
        this.description = description;
        this.time = time;
    }
    public String getProcessingElementID() {
        return processingElementID;
    }

    public void setProcessingElementID(String processingElementID) {
        this.processingElementID = processingElementID;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}