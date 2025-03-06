package com.dapm.security_service.models;

import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "access_request")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRequest {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id; // Manually set

    // The user who made the access request.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // Reference the ResourceType entity.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType resourceType;

    // Reference the Action entity.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_type_id", nullable = false)
    private ActionType action;

    @Column(name = "details", length = 1000)
    private String details;

    // ABAC attribute: requested duration (in hours)
    @Column(name = "duration_in_hours")
    private Long durationInHours;

    @Column(name = "request_time", nullable = false)
    private Instant requestTime;

    @Column(name = "decision_time")
    private Instant decisionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccessRequestStatus status;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.requestTime = Instant.now(); // Set to UTC time on creation
    }
}
