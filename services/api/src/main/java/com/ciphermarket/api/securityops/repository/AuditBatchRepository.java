package com.ciphermarket.api.securityops.repository;

import com.ciphermarket.api.securityops.domain.AuditBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditBatchRepository extends JpaRepository<AuditBatch, UUID> {

    Optional<AuditBatch> findTopByOrderBySealedAtDesc();

    List<AuditBatch> findTop50ByOrderBySealedAtDesc();
}
