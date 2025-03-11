package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Node;
import com.dapm.security_service.models.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, UUID> { }