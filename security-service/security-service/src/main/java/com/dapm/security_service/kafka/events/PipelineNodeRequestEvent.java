package com.dapm.security_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineNodeRequestEvent {
    private UUID requestId;
    private UUID pipelineId;
    private UUID pipelineNodeId;
    private String requesterUsername;
    private String requesterFaculty;
    private String requesterDepartment;
    private String requesterOrganization;
    private int requestedExecutionCount;
    private int requestedDurationHours;
    private int requestedDataUsagePercentage;
}
