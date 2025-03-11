package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;
@Data

public class PipelineNodeRequestDto {
    private UUID id;
    private UUID pipelineNode;
    private UserDto requester;
    private int requestedExecutionCount;
    private int requestedDurationHours;
    private AccessRequestStatus status;
    private String approvalToken;
    private Instant decisionTime;
    public PipelineNodeRequestDto(PipelineNodeRequest pipelineNodeRequest){
        this.id=pipelineNodeRequest.getId();
        this.pipelineNode=pipelineNodeRequest.getPipelineNode().getId();
        UserDto userDto=new UserDto(pipelineNodeRequest.getRequester());
        this.requester=userDto;
        this.requestedExecutionCount= pipelineNodeRequest.getRequestedExecutionCount();
        this.requestedDurationHours= pipelineNodeRequest.getRequestedDurationHours();
        this.status=pipelineNodeRequest.getStatus();
        this.decisionTime=pipelineNodeRequest.getDecisionTime();
        this.approvalToken=pipelineNodeRequest.getApprovalToken();
    }
}
