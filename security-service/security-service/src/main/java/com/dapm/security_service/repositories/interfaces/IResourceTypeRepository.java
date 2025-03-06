package com.dapm.security_service.repositories.interfaces;

import com.dapm.security_service.models.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface IResourceTypeRepository extends JpaRepository<ResourceType, UUID> {
    ResourceType findByName(String name);
}