package com.dapm.security_service.repositories;

import com.dapm.security_service.models.OrgPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrgPermissionRepository extends JpaRepository<OrgPermission, UUID> {
}

