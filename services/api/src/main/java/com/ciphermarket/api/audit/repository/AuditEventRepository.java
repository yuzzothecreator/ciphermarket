package com.ciphermarket.api.audit.repository;

import com.ciphermarket.api.audit.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    @Query(value = "SELECT * FROM audit_events ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<AuditEvent> findLatest();
}
