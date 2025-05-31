package pipeline.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import communication.API.HTTPClient;
import communication.API.request.HTTPRequest;
import communication.API.response.HTTPResponse;
import utils.JsonUtil;
import utils.LogUtil;

@Service
public class DefaultPipelineNotificationService implements PipelineNotificationService {
    private final HTTPClient httpClient;

    @Autowired
    public DefaultPipelineNotificationService(HTTPClient httpClient) {
        this.httpClient = httpClient;
    }    

    // TODO: REFACTOR
    // TODO: By injecting the PipelineRepository, we can have access to the PE REF which contains the organizationHostURL
    @Override
    public void sendNotification(PipelineNotification notification, String organizationHostURL) {
        LogUtil.info("[PIPELINE NOTIFICATION] Type: {}, PipelineID: {}, ReportingPE: {}, Message: \"{}\", Details: {}",
                notification.notificationType(),
                notification.pipelineId() != null ? notification.pipelineId() : "N/A",
                notification.reportingPeInstanceId(),
                notification.message(),
                notification.details().isEmpty() ? "{}" : JsonUtil.toJson(notification.details())
        );

        String url = organizationHostURL + "/pipeline/notification";
        try {
            String notificationJson = JsonUtil.toJson(notification);            
            HTTPRequest request = new HTTPRequest(url, notificationJson);
            HTTPResponse response = httpClient.postSync(request);

            if (response != null && response.status().is2xxSuccessful()) {
                LogUtil.info("[NOTIFY SVC SUCCESS] Notification sent successfully to {}. Response: {} {}",
                        url, response.status(), response.body() != null ? response.body() : "");
            } else {
                String statusCode = response != null ? response.status().toString() : "N/A";
                String responseBody = response != null && response.body() != null ? response.body() : "N/A";
                LogUtil.info("[NOTIFY SVC FAIL] Failed to send notification to {}. Status: {}, Body: {}",
                        url, statusCode, responseBody);
            }
        } catch (Exception e) {
            LogUtil.error(e, "[NOTIFY SVC ERR] Error sending notification to {}: {}", url, notification);
        }
    }

}
