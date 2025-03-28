package com.dapm.security_service.repositories;

import com.dapm.security_service.models.PipelineNodeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PipelineNodeRequestRepository extends JpaRepository<PipelineNodeRequest, UUID> { }
