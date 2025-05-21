package com.dapm.security_service.repositories;

import com.dapm.security_service.models.OrgPermission;
import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.enums.OrgPermAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrgPermissionRepository extends JpaRepository<OrgPermission, UUID> {
    OrgPermission findByAction(OrgPermAction action);
}

