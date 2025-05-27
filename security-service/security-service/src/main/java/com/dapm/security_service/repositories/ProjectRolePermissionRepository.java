package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.ProjectPermission;
import com.dapm.security_service.models.ProjectRole;
import com.dapm.security_service.models.ProjectRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRolePermissionRepository extends JpaRepository<ProjectRolePermission, UUID> {
    Optional<ProjectRolePermission> findByProjectAndPermissionAndRole(Project project, ProjectPermission permission, ProjectRole role);
}
