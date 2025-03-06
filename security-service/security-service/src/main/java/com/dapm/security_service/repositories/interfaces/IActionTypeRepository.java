package com.dapm.security_service.repositories.interfaces;

import com.dapm.security_service.models.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IActionTypeRepository extends JpaRepository<ActionType, UUID> {
    ActionType findByName(String name);
}
