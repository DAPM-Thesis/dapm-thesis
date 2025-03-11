package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.User;
import com.dapm.security_service.models.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String organization;
    private String faculty;
    private String department;
    private Set<String> roles;
    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.organization = (user.getOrganization() != null) ? user.getOrganization().getName() : null;
        this.faculty = (user.getFaculty() != null) ? user.getFaculty().getName() : null;
        this.department = (user.getDepartment() != null) ? user.getDepartment().getName() : null;
        this.roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
