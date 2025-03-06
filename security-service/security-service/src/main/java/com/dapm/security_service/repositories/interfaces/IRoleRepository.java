package com.dapm.security_service.repositories.interfaces;

import com.dapm.security_service.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IRoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(String name);
}
