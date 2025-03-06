package com.dapm.security_service.repositories.interfaces;

import com.dapm.security_service.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IOrganizationRepository extends JpaRepository<Organization, UUID> { }
