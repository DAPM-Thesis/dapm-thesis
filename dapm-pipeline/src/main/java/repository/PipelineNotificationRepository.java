package repository;

import org.springframework.stereotype.Repository;
import pipeline.notification.PipelineNotification; // Assuming PipelineNotification DTO is in pipeline.notification

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class PipelineNotificationRepository {

    private final Map<String, List<PipelineNotification>> notificationsByPipeline = new ConcurrentHashMap<>();
    private final List<PipelineNotification> allNotificationsList = new CopyOnWriteArrayList<>();

    public void storeNotification(PipelineNotification notification) {
        if (notification == null) {
            return;
        }

        String pipelineIdKey = notification.pipelineId() != null ? notification.pipelineId() : "_UNKNOWN_PIPELINE_";

        notificationsByPipeline.computeIfAbsent(
                pipelineIdKey,
                k -> new CopyOnWriteArrayList<>() 
        ).add(notification);

        allNotificationsList.add(notification);
    }

    public List<PipelineNotification> getNotificationsByPipelineId(String pipelineId) {
        if (pipelineId == null) {
            return Collections.emptyList();
        }
        List<PipelineNotification> notifications = notificationsByPipeline.get(pipelineId);
        if (notifications == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(notifications)); // Return a copy
    }

    public List<PipelineNotification> getAllNotifications() {
        return Collections.unmodifiableList(new ArrayList<>(allNotificationsList)); // Return a copy
    }

    public void clearNotificationsForPipeline(String pipelineId) {
        if (pipelineId != null) {
            List<PipelineNotification> removedFromPipelineMap = notificationsByPipeline.remove(pipelineId);
            if (removedFromPipelineMap != null) {
                // Also remove them from the global list
                // This can be inefficient for large lists; consider alternatives if performance is critical
                allNotificationsList.removeAll(removedFromPipelineMap);
            }
        }
    }

    public void clearAllNotifications() {
        notificationsByPipeline.clear();
        allNotificationsList.clear();
    }

    public int countNotificationsForPipeline(String pipelineId) {
        if (pipelineId == null) return 0;
        return notificationsByPipeline.getOrDefault(pipelineId, Collections.emptyList()).size();
    }

    public int countAllNotifications() {
        return allNotificationsList.size();
    }
}