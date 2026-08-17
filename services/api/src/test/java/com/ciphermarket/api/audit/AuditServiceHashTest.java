package com.ciphermarket.api.audit;

import com.ciphermarket.api.audit.service.AuditService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditServiceHashTest {

    private final AuditService auditService = new AuditService(null, new com.fasterxml.jackson.databind.ObjectMapper());

    @Test
    void computeHash_isDeterministic() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String hash1 = auditService.computeHash(id, "TEST", "resource", id, "corr-1", "GENESIS");
        String hash2 = auditService.computeHash(id, "TEST", "resource", id, "corr-1", "GENESIS");
        assertThat(hash1).isEqualTo(hash2).hasSize(64);
    }

    @Test
    void computeHash_changesWhenPreviousHashChanges() {
        UUID id = UUID.randomUUID();
        String hash1 = auditService.computeHash(id, "TEST", "resource", id, "corr-1", "GENESIS");
        String hash2 = auditService.computeHash(id, "TEST", "resource", id, "corr-1", hash1);
        assertThat(hash1).isNotEqualTo(hash2);
    }
}
