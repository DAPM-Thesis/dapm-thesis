package com.dapm.security_service.models.dtos;

import lombok.Data;
import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Role;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class PipelineDto {
    private UUID id;
    private String name;

    // Instead of returning the full Organization, we return its ID and name.
    private UUID ownerOrganizationId;
    private String ownerOrganizationName;

    private String description;

    // Instead of the full Role, return its ID and name.
    private UUID pipelineRoleId;
    private String pipelineRoleName;

    // If you want to include nodes and tokens, you can return just their IDs.
    private Set<UUID> nodeIds;
    private Set<UUID> tokenIds;

    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    // Default constructor.
    public PipelineDto() {}

    // Constructor to create a DTO from a Pipeline entity.
    public PipelineDto(Pipeline pipeline) {
        this.id = pipeline.getId();
        this.name = pipeline.getName();

        Organization ownerOrg = pipeline.getOwnerOrganization();
        if (ownerOrg != null) {
            this.ownerOrganizationId = ownerOrg.getId();
            this.ownerOrganizationName = ownerOrg.getName();
        }

        this.description = pipeline.getDescription();

        Role role = pipeline.getPipelineRole();
        if (role != null) {
            this.pipelineRoleId = role.getId();
            this.pipelineRoleName = role.getName();
        }

        if (pipeline.getNodes() != null) {
            this.nodeIds = pipeline.getNodes()
                    .stream()
                    .map(node -> node.getId())
                    .collect(Collectors.toSet());
        }

        if (pipeline.getTokens() != null) {
            this.tokenIds = pipeline.getTokens()
                    .stream()
                    .map(token -> token.getId())
                    .collect(Collectors.toSet());
        }

        this.createdBy = pipeline.getCreatedBy();
        this.createdAt = pipeline.getCreatedAt();
        this.updatedAt = pipeline.getUpdatedAt();
    }
}
