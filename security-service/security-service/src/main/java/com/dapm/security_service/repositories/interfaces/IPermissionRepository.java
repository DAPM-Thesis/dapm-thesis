package com.dapm.security_service.repositories.interfaces;

import com.dapm.security_service.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IPermissionRepository extends JpaRepository<Permission, UUID> {
    Permission findByName(String name);
}