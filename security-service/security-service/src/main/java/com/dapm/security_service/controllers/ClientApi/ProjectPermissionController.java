package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.ProjectPermission;
import com.dapm.security_service.repositories.ProjPermissionRepository;
import com.dapm.security_service.repositories.ProjectRepository;
import com.dapm.security_service.repositories.ProjectRolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dapm.security_service.models.dtos.ProjectPermissionDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/project-permissions")
public class ProjectPermissionController {
    @Autowired
    private ProjPermissionRepository projPermissionRepository;


    @GetMapping
    public List<ProjectPermissionDto> getAllPermissions() {
        return projPermissionRepository.findAll()
                .stream()
                .map(ProjectPermissionDto::new)
                .toList();
    }
    @PostMapping
    public ResponseEntity<ProjectPermissionDto> createPermission(@RequestBody ProjectPermissionDto request) {
        ProjectPermission permission = ProjectPermission.builder()
                .id(UUID.randomUUID())
                .action(request.getAction())
                .scope(request.getScope())
                .build();
        permission = projPermissionRepository.save(permission);
        return ResponseEntity.ok(new ProjectPermissionDto(permission));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable UUID id) {
        projPermissionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
