package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.Role;
import com.dapm.security_service.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
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
    public Role createRole(@RequestBody Role role) {
        if (role.getId() == null) {
            role.setId(UUID.randomUUID());
        }
        return roleRepository.save(role);
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
