package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Organization;
import com.dapm.security_service.repositories.interfaces.IOrganizationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public class OrganizationRepository {
    // Spring Data JPA automatically implements the CRUD methods.
}
