package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Permission;
import com.dapm.security_service.models.Role;
import lombok.Data;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class RoleDto {
    private UUID id;
    private String name;
    private Organization organization;
    private Set<String> permissions;
    public RoleDto(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.organization = role.getOrganization();
        this.permissions = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}
