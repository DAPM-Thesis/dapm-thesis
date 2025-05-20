package com.dapm.security_service.models;

import com.dapm.security_service.models.enums.OrgPermAction;
import com.dapm.security_service.models.enums.ProjectPermAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "project_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPermission {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "proj_action", nullable = false)
    private ProjectPermAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id")
    private Pipeline pipeline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processing_element_id")
    private ProcessingElement processingElement;


//    Validates that only one target is set: either pipeline or processing element.
    public boolean hasValidTarget() {
        return (pipeline != null) ^ (processingElement != null); // XOR logic
    }
}
