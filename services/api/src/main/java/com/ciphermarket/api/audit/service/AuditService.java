package com.ciphermarket.api.audit.service;

import com.ciphermarket.api.audit.domain.AuditEvent;
import com.ciphermarket.api.audit.repository.AuditEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository auditEventRepository, ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuditEvent record(
            UUID organisationId,
            UUID actorUserId,
            String actorKeycloakSub,
            String action,
            String resourceType,
            UUID resourceId,
            Map<String, Object> beforeSummary,
            Map<String, Object> afterSummary
    ) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        String previousHash = auditEventRepository.findLatest()
                .map(AuditEvent::getEventHash)
                .orElse("GENESIS");

        UUID eventId = UUID.randomUUID();
        String eventHash = computeHash(eventId, action, resourceType, resourceId, correlationId, previousHash);

        AuditEvent event = AuditEvent.create(
                eventId,
                organisationId,
                actorUserId,
                actorKeycloakSub,
                action,
                resourceType,
                resourceId,
                beforeSummary,
                afterSummary,
                correlationId,
                eventHash,
                previousHash
        );

        return auditEventRepository.save(event);
    }

    public String computeHash(
            UUID eventId,
            String action,
            String resourceType,
            UUID resourceId,
            String correlationId,
            String previousHash
    ) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String payload = eventId + "|" + action + "|" + resourceType + "|" + resourceId
                    + "|" + correlationId + "|" + previousHash;
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public String serializeForHash(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
