// ** TODO: move the token to vault
package com.dapm.security_service.models;

import com.dapm.security_service.models.RequesterInfo;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pipeline_node_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineNodeRequest {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_node_id", nullable = false)
    private Node pipelineNode;

    // Instead of ManyToOne User, we store a snapshot:
    @Embedded
    private RequesterInfo requesterInfo;

    @Column(name = "requested_execution_count")
    private int requestedExecutionCount;

    @Column(name = "requested_duration_hours")
    private int requestedDurationHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccessRequestStatus status;

    @Column(name = "approval_token", length = 4096)
    private String approvalToken;

    @Column(name = "decision_time")
    private Instant decisionTime;
}
