package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.Faculty;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.UUID;
import com.dapm.security_service.models.Faculty;
import com.dapm.security_service.models.Department;
import lombok.Data;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class DepartmentDto {
    private UUID id;

    private String name;

    private String faculty;

    public DepartmentDto(Department dep){
        this.id=dep.getId();
        this.name= dep.getName();
        this.faculty=dep.getFaculty().getName();
    }
}
