package com.dapm.security_service.repositories;

import com.dapm.security_service.models.PipelineProcessingElementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PipelineNodeRequestRepository extends JpaRepository<PipelineProcessingElementRequest, UUID> { }
