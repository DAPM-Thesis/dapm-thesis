package com.dapm.security_service.repositories.interfaces;

import com.dapm.security_service.models.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IAccessRequestRepository extends JpaRepository<AccessRequest, UUID> {
    List<AccessRequest> findByRequesterId(UUID requesterId);
}
