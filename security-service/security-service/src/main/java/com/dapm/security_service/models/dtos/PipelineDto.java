package com.dapm.security_service.models.dtos;

import lombok.Data;
import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Role;
import com.dapm.security_service.models.Node;
import com.dapm.security_service.models.Token;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class PipelineDto {
    private UUID id;
    private String name;

    private UUID ownerOrganizationId;
    private String ownerOrganizationName;

    private String description;

    private UUID pipelineRoleId;
    private String pipelineRoleName;

    private Set<UUID> nodeIds;
    private Set<UUID> tokenIds;

    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public PipelineDto() {}

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

        // Convert nodes collection to a set of IDs
        this.nodeIds = pipeline.getNodes() != null
                ? pipeline.getNodes().stream().map(Node::getId).collect(Collectors.toSet())
                : Collections.emptySet();

        // Convert tokens collection to a set of IDs
        this.tokenIds = pipeline.getTokens() != null
                ? pipeline.getTokens().stream().map(Token::getId).collect(Collectors.toSet())
                : Collections.emptySet();

        this.createdBy = pipeline.getCreatedBy();
        this.createdAt = pipeline.getCreatedAt();
        this.updatedAt = pipeline.getUpdatedAt();
    }
}
