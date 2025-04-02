package com.dapm.security_service.repositories;

import com.dapm.security_service.models.PeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeTemplateRepository extends JpaRepository<PeTemplate, String> {
    // Find all PE Templates owned by a specific organization
    List<PeTemplate> findByOwner(String owner);

    // Find all PE Templates that have a specific organization in their visibility set
    List<PeTemplate> findByVisibilityContaining(String org);
}
