package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.AccessRequest;
import com.dapm.security_service.repositories.AccessRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/access-requests")
public class AccessRequestController {

    @Autowired
    private AccessRequestRepository accessRequestRepository;

    @GetMapping
    public List<AccessRequest> getAllAccessRequests() {
        return accessRequestRepository.findAll();
    }

    @GetMapping("/{id}")
    public AccessRequest getAccessRequestById(@PathVariable UUID id) {
        return accessRequestRepository.findById(id).orElse(null);
    }

    @PostMapping
    public AccessRequest createAccessRequest(@RequestBody AccessRequest accessRequest) {
        if (accessRequest.getId() == null) {
            accessRequest.setId(UUID.randomUUID());
        }
        return accessRequestRepository.save(accessRequest);
    }

    @PutMapping("/{id}")
    public AccessRequest updateAccessRequest(@PathVariable UUID id, @RequestBody AccessRequest accessRequest) {
        accessRequest.setId(id);
        return accessRequestRepository.save(accessRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteAccessRequest(@PathVariable UUID id) {
        accessRequestRepository.deleteById(id);
    }
}
