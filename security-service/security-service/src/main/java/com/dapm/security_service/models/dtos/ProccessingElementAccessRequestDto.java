package com.dapm.security_service.models.dtos;
import com.dapm.security_service.models.*;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProccessingElementAccessRequestDto {
    private String processingElement;
    private String pipelineName;
    private RequesterInfo requesterInfo;
    private String webhookUrl;

    public ProccessingElementAccessRequestDto(PipelineProcessingElementRequest request) {
        this.processingElement = request.getPipelineNode().getTemplateId();
        this.pipelineName = request.getPipelineId().toString();
        this.requesterInfo = request.getRequesterInfo();
        this.webhookUrl = request.getWebhookUrl();
    }
}
