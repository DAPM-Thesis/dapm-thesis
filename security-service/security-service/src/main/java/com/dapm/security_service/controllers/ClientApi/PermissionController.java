package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.Permission;
import com.dapm.security_service.repositories.interfaces.IPermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private IPermissionRepository permissionRepository;

    @GetMapping
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @GetMapping("/{id}")
    public Permission getPermissionById(@PathVariable UUID id) {
        return permissionRepository.findById(id).orElse(null);
    }

    @GetMapping("/name/{name}")
    public Permission getPermissionByName(@PathVariable String name) {
        return permissionRepository.findByName(name);
    }

    @PostMapping
    public Permission createPermission(@RequestBody Permission permission) {
        if (permission.getId() == null) {
            permission.setId(UUID.randomUUID());
        }
        return permissionRepository.save(permission);
    }

    @PutMapping("/{id}")
    public Permission updatePermission(@PathVariable UUID id, @RequestBody Permission permission) {
        permission.setId(id);
        return permissionRepository.save(permission);
    }

    @DeleteMapping("/{id}")
    public void deletePermission(@PathVariable UUID id) {
        permissionRepository.deleteById(id);
    }
}
