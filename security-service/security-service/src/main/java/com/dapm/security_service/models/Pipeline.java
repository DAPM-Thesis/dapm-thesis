package com.dapm.security_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
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
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_organization_id", nullable = false)
    private Organization ownerOrganization;

    @Column(name = "description", length = 1000)
    private String description;

    // Pipeline execution role
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pipeline_role_id", nullable = false)
    private Role pipelineRole;

    @ManyToMany
    @JoinTable(
            name = "pipeline_node_mapping",
            joinColumns = @JoinColumn(name = "pipeline_id"),
            inverseJoinColumns = @JoinColumn(name = "node_id")
    )
    @Builder.Default
    private Set<Node> nodes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Token> tokens = new HashSet<>();

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
