package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.enums.AccessRequestStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PipelineNodeRequestDto {
    private UUID id;

    private UUID pipelineNodeId;
    private UUID requesterId;

    private int requestedExecutionCount;
    private int requestedDurationHours;
    private AccessRequestStatus status;

    private String approvalToken;
    private Instant decisionTime;
}