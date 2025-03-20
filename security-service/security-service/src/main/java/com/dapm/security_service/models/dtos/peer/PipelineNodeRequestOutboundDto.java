package com.dapm.security_service.models.dtos.peer;

import com.dapm.security_service.models.enums.AccessRequestStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound DTO for sending PipelineNodeRequest data to OrgB.
 */
@Data
public class PipelineNodeRequestOutboundDto {
    private UUID id;
    private UUID pipelineNodeId;
    private RequesterInfoDto requesterInfo;
    private UUID pipelineId;

    private int requestedExecutionCount;
    private int requestedDurationHours;
    private AccessRequestStatus status;

    private String approvalToken;
    private Instant decisionTime;
    private String webhookUrl;
}
