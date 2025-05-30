package pipeline.notification;

public interface PipelineNotificationService {
    void sendNotification(PipelineNotification notification, String organizationHostURL);    
} 