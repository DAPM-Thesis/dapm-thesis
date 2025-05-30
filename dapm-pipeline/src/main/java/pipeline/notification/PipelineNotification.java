package pipeline.notification;

import java.time.Instant;
import java.util.Map;
import java.util.Collections;

public record PipelineNotification(
    NotificationType notificationType,
    String pipelineId,
    String reportingPeInstanceId,
    String message,
    Instant timestamp,
    Map<String, Object> details
) {
    public PipelineNotification {
        details = details != null ? Collections.unmodifiableMap(details) : Collections.emptyMap();
    }
}
