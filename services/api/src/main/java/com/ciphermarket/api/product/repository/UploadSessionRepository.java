package com.ciphermarket.api.product.repository;

import com.ciphermarket.api.product.domain.UploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UploadSessionRepository extends JpaRepository<UploadSession, UUID> {

    Optional<UploadSession> findByIdAndOrganisationId(UUID id, UUID organisationId);
}
