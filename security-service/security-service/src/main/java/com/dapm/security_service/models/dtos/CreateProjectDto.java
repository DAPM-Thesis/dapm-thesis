package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.Project;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateProjectDto {

    private String title;


    public CreateProjectDto(Project project) {
        this.title = project.getTitle();

    }


}
