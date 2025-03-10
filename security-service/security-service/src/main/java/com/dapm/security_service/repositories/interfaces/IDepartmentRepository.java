package com.dapm.security_service.repositories.interfaces;

import com.dapm.security_service.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IDepartmentRepository extends JpaRepository<Department, UUID> {
    Department findByName(String name);
}
