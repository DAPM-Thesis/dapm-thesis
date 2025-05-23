package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.enums.AccessRequestStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PipelineProcessingElementRequestDto {
    private UUID id;

    private UUID pipelinePeId;
    private UUID requesterId;

    private UUID pipelineId;

    private int requestedExecutionCount;
    private int requestedDurationHours;
    private AccessRequestStatus status;

    private String approvalToken;
    private Instant decisionTime;
    private String webhookUrl;
}