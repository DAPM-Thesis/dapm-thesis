package com.dapm.security_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "pipeline_node")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Node {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id; // Manually set

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_organization_id", nullable = false)
    private Organization ownerOrganization;

    @ManyToMany
    @JoinTable(
            name = "pipeline_node_resources",
            joinColumns = @JoinColumn(name = "pipeline_node_id"),
            inverseJoinColumns = @JoinColumn(name = "resource_id")
    )
    private Set<Resource> allowedResources;

    @Column(name = "default_execution_count")
    private int defaultExecutionCount;

    @Column(name = "default_duration_hours")
    private int defaultDurationHours;
}
