package controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pipeline.notification.PipelineNotification;
import repository.PipelineNotificationRepository;
import utils.LogUtil;

@RestController
@RequestMapping("/pipeline")
public class NotificationController {

    private final PipelineNotificationRepository notificationRepository;

    @Autowired
    public NotificationController(PipelineNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostMapping("/notification")
    public ResponseEntity<Void> submitNotification(@RequestBody PipelineNotification notification) {
        if (notification == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            LogUtil.info("[NOTIFICATION CTRL] Received notification: Type={}, PipelineID={}, ReportingPE={}, Message=\"{}\"",
                    notification.notificationType(),
                    notification.pipelineId(),
                    notification.reportingPeInstanceId(),
                    notification.message());
            notificationRepository.storeNotification(notification);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            LogUtil.error(e, "[NOTIFICATION CTRL ERR] Failed to store notification: {}", notification);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{pipelineId}/notifications")
    public ResponseEntity<List<PipelineNotification>> getNotificationsByPipeline(
            @PathVariable String pipelineId) {
        if (pipelineId == null || pipelineId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        try {
            List<PipelineNotification> notifications = notificationRepository.getNotificationsByPipelineId(pipelineId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            LogUtil.error(e, "[NOTIFICATION CTRL ERR] Failed to retrieve notifications for PipelineID: {}", pipelineId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/notifications/all") 
    public ResponseEntity<List<PipelineNotification>> getAllNotifications() {
        try {
            List<PipelineNotification> notifications = notificationRepository.getAllNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            LogUtil.error(e, "[NOTIFICATION CTRL ERR] Failed to retrieve all notifications.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}