package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Node;
import com.dapm.security_service.models.Pipeline;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, UUID> {
    @EntityGraph(value = "Pipeline.nodesAndTokens", type = EntityGraph.EntityGraphType.FETCH)
    List<Pipeline> findAll();

    // Fetch all pipelines, including nodes & tokens, in one go
    @Query("SELECT DISTINCT p FROM Pipeline p "
            + "LEFT JOIN FETCH p.nodes "
            + "LEFT JOIN FETCH p.tokens")
    List<Pipeline> findAllWithNodesAndTokens();

    // Fetch a single pipeline by ID, including nodes & tokens
    @Query("SELECT DISTINCT p FROM Pipeline p "
            + "LEFT JOIN FETCH p.nodes "
            + "LEFT JOIN FETCH p.tokens "
            + "WHERE p.id = :id")
    Optional<Pipeline> findByIdWithNodesAndTokens(@Param("id") UUID id);
}