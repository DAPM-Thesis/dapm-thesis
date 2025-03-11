package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.*;
import lombok.Data;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.*;
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

        Set<Node> nodes = pipeline.getNodes();
        this.nodeIds = (nodes != null)
                ? nodes.stream().map(Node::getId).collect(Collectors.toSet())
                : Collections.emptySet();

        // Handle tokens
        Set<Token> tokens = pipeline.getTokens();
        this.tokenIds = (tokens != null)
                ? tokens.stream().map(Token::getId).collect(Collectors.toSet())
                : Collections.emptySet();

        this.createdBy = pipeline.getCreatedBy();
        this.createdAt = pipeline.getCreatedAt();
        this.updatedAt = pipeline.getUpdatedAt();
    }

}
