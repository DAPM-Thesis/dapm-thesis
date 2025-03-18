package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    Faculty findByName(String name);
}