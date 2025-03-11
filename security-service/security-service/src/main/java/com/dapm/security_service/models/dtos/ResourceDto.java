package com.dapm.security_service.models.dtos;

import java.util.UUID;

import com.dapm.security_service.models.Resource;

import lombok.Data;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class ResourceDto {
    private UUID id;
    private String name;
    private String description;
    private UUID resourceType;


    private String organization;

    public ResourceDto(Resource res){
        this.id=res.getId();
        this.name=res.getName();
        this.description=res.getDescription();
        this.resourceType=res.getResourceType().getId();
        this.organization=res.getOrganization().getName();
    }
}
