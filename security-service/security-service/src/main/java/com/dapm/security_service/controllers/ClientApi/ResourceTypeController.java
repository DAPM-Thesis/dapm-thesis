package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.ResourceType;
import com.dapm.security_service.repositories.interfaces.IResourceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resource-types")
public class ResourceTypeController {

    @Autowired
    private IResourceTypeRepository resourceTypeRepository;

    @GetMapping
    public List<ResourceType> getAllResourceTypes() {
        return resourceTypeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResourceType getResourceTypeById(@PathVariable UUID id) {
        return resourceTypeRepository.findById(id).orElse(null);
    }

    @GetMapping("/name/{name}")
    public ResourceType getResourceTypeByName(@PathVariable String name) {
        return resourceTypeRepository.findByName(name);
    }

    @PostMapping
    public ResourceType createResourceType(@RequestBody ResourceType resourceType) {
        if (resourceType.getId() == null) {
            resourceType.setId(UUID.randomUUID());
        }
        return resourceTypeRepository.save(resourceType);
    }

    @PutMapping("/{id}")
    public ResourceType updateResourceType(@PathVariable UUID id, @RequestBody ResourceType resourceType) {
        resourceType.setId(id);
        return resourceTypeRepository.save(resourceType);
    }

    @DeleteMapping("/{id}")
    public void deleteResourceType(@PathVariable UUID id) {
        resourceTypeRepository.deleteById(id);
    }
}
