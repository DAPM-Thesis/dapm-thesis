package com.dapm.security_service.models.dtos;

import lombok.Data;
import com.dapm.security_service.models.Node;
import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Resource;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class NodeDto {
    private UUID id;
    private String name;
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
        } else {
            this.allowedResourceIds = Collections.emptySet();
        }

        this.defaultExecutionCount = node.getDefaultExecutionCount();
        this.defaultDurationHours = node.getDefaultDurationHours();
    }
}
