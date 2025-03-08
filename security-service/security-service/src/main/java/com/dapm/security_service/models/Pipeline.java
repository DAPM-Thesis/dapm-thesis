package com.dapm.security_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pipeline")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pipeline {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id; // Must be manually set

    @Column(name = "name", nullable = false)
    private String name;

    // Each pipeline is owned by an organization.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_organization_id", nullable = false)
    private Organization ownerOrganization;

    @Column(name = "description", length = 1000)
    private String description;

    // Pipeline execution role (a Role dedicated to controlling pipeline execution)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_role_id", nullable = false)
    private Role pipelineRole;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId(){
        return id;
    }

    public void setId(UUID id){
        this.id = id;
    }
}
