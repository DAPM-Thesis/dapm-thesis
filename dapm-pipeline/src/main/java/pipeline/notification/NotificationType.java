package pipeline.notification;

public enum NotificationType {
    HEARTBEAT_TIMEOUT_ON_UPSTREAM_TOPIC,
    HEARTBEAT_TIMEOUT_ON_DOWNSTREAM_TOPIC,
    PE_SELF_DETECTED_ERROR // General PE error, e.g., lost permission
}