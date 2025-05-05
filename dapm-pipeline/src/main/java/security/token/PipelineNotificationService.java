package security.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PipelineNotificationService {
    private final Map<String, Queue<PipelineNotification>> pipelineNotifications = new ConcurrentHashMap<>();
    @Autowired
    public PipelineNotificationService(){}

    public void record(PipelineNotification notification) {
        pipelineNotifications
                .computeIfAbsent(notification.getProcessingElementID(), k -> new ConcurrentLinkedQueue<>())
                .add(notification);
        System.out.println("NOTIFICATION: PE-ID: "+ notification.getProcessingElementID() + " DESCRIPTION: " + notification.getDescription());
    }

    public List<PipelineNotification> get(String processingElementID) {
        Queue<PipelineNotification> queue = pipelineNotifications.get(processingElementID);
        return queue == null ? List.of() : List.copyOf(queue);
    }

    public List<PipelineNotification> getAll() {
        List<PipelineNotification> all = new ArrayList<>();
        for (Queue<PipelineNotification> queue : pipelineNotifications.values()) {
            all.addAll(queue);
        }
        return all;
    }

}
