package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.Faculty;

import lombok.Data;

import java.util.UUID;

@Data
public class FacultyDto {
    private UUID id;

    private String name;


    private String organization;
    public FacultyDto (Faculty faculty){
        this.id=faculty.getId();
        this.name=faculty.getName();
        this.organization=faculty.getOrganization().getName();
    }
}
