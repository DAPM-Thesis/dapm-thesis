package com.dapm.security_service.controllers;

import com.dapm.security_service.models.Organization;
import com.dapm.security_service.repositories.interfaces.IOrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    @Autowired
    private IOrganizationRepository organizationRepository;

    @GetMapping
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @GetMapping("/{id}")
    public Organization getOrganizationById(@PathVariable UUID id) {
        return organizationRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Organization createOrganization(@RequestBody Organization organization) {
        if (organization.getId() == null) {
            organization.setId(UUID.randomUUID());
        }
        return organizationRepository.save(organization);
    }

    @PutMapping("/{id}")
    public Organization updateOrganization(@PathVariable UUID id, @RequestBody Organization organization) {
        organization.setId(id);
        return organizationRepository.save(organization);
    }

    @DeleteMapping("/{id}")
    public void deleteOrganization(@PathVariable UUID id) {
        organizationRepository.deleteById(id);
    }
}
