package com.dapm.security_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineNodeApprovalEvent {
    private UUID requestId;
    private String approvalToken;
    private String status; // e.g., "APPROVED" or "REJECTED"
}
