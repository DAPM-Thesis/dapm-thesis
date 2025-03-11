package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Node;
import com.dapm.security_service.models.Pipeline;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, UUID> {
    @EntityGraph(value = "Pipeline.nodesAndTokens", type = EntityGraph.EntityGraphType.FETCH)
    List<Pipeline> findAll();
}