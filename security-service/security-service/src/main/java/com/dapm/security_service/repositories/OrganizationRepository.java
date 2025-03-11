package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> { 
    Organization findByName(String name);
}
