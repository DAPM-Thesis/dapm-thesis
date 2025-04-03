package com.dapm.security_service.repositories;

import com.dapm.security_service.models.ProcessingElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessingElementRepository extends JpaRepository<ProcessingElement, UUID> {
}
