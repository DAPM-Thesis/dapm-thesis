package com.dapm.security_service.repositories.interfaces;

import com.dapm.security_service.models.Permission;
import com.dapm.security_service.models.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IPolicyRepository extends JpaRepository<Policy, UUID> {
    Policy findByPermission(Permission permission);
}