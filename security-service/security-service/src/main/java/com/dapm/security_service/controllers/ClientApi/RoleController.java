package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Permission;
import com.dapm.security_service.models.Role;
import com.dapm.security_service.models.dtos.RoleDto;
import com.dapm.security_service.repositories.OrganizationRepository;
import com.dapm.security_service.repositories.PermissionRepository;
import com.dapm.security_service.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @GetMapping
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream().map(RoleDto::new).toList();
    }

    @GetMapping("/{id}")
    public Role getRoleById(@PathVariable UUID id) {
        return roleRepository.findById(id).orElse(null);
    }

    @GetMapping("/name/{name}")
    public Role getRoleByName(@PathVariable String name) {
        return roleRepository.findByName(name);
    }

    @PostMapping
    public Role createRole(@RequestBody RoleDto roleDto) {
        if (roleDto.getId() == null) {
            roleDto.setId(UUID.randomUUID());
        }
        Role r=new Role();
        r.setId(roleDto.getId());
        r.setName(roleDto.getName());

        Organization organization = organizationRepository.findById(roleDto.getOrganization().getId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        r.setOrganization(organization);

        //here we are changing set of string permissions to set od permissions

        Set<Permission> permissions = roleDto.getPermissions().stream()
                .map(permissionName -> {
                    Permission permission = permissionRepository.findByName(permissionName);
                    if (permission == null) { // If not found, create a new one
                        permission = new Permission();
                        permission.setName(permissionName);
                        permission = permissionRepository.save(permission); // Save new permission
                    }
                    return permission;
                })
                .collect(Collectors.toSet());

        r.setPermissions(permissions);

        return roleRepository.save(r);
    }

    @PutMapping("/{id}")
    public Role updateRole(@PathVariable UUID id, @RequestBody Role role) {
        role.setId(id);
        return roleRepository.save(role);
    }

    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable UUID id) {
        roleRepository.deleteById(id);
    }
}
