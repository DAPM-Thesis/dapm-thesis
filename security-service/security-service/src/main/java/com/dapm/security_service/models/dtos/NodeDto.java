package com.dapm.security_service.models.dtos;

import lombok.Data;
import com.dapm.security_service.models.Node;
import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.models.Resource;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class NodeDto {
    private UUID id;
    private String name;
    private Set<UUID> pipelineIds;
    private UUID ownerOrganizationId;
    private String ownerOrganizationName;
    private Set<UUID> allowedResourceIds;
    private int defaultExecutionCount;
    private int defaultDurationHours;

    public NodeDto() {
    }

    public NodeDto(Node node) {
        this.id = node.getId();
        this.name = node.getName();

        // Use the many-to-many pipelines collection.
        if (node.getPipelines() != null) {
            this.pipelineIds = node.getPipelines()
                    .stream()
                    .map(Pipeline::getId)
                    .collect(Collectors.toSet());
        }

        Organization org = node.getOwnerOrganization();
        if (org != null) {
            this.ownerOrganizationId = org.getId();
            this.ownerOrganizationName = org.getName();
        }

        if (node.getAllowedResources() != null) {
            this.allowedResourceIds = node.getAllowedResources()
                    .stream()
                    .map(Resource::getId)
                    .collect(Collectors.toSet());
        }

        this.defaultExecutionCount = node.getDefaultExecutionCount();
        this.defaultDurationHours = node.getDefaultDurationHours();
    }
}
