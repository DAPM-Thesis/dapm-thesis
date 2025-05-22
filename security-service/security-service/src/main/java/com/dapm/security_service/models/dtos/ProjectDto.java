package com.dapm.security_service.models.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import  com.dapm.security_service.models.Project;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ProjectDto {
    private UUID id;
    private String title;
    private String organizationName;

    public ProjectDto(Project project) {
        this.id = project.getId();
        this.title = project.getTitle();
        this.organizationName = project.getOrganization().getName();
    }
}
