package com.dapm.security_service.repositories.interfaces;

import com.dapm.security_service.models.Permission;
import com.dapm.security_service.models.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface IPolicyRepository extends JpaRepository<Policy, UUID> {
    Policy findByPermission(Permission permission);
}