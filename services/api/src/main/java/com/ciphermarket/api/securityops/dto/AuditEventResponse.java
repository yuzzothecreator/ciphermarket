package com.ciphermarket.api.securityops.dto;

import com.ciphermarket.api.audit.domain.AuditEvent;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        UUID organisationId,
        UUID actorUserId,
        String actorKeycloakSub,
        String action,
        String resourceType,
        UUID resourceId,
        String correlationId,
        String eventHash,
        String previousHash,
        Instant createdAt
) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getOrganisationId(),
                event.getActorUserId(),
                event.getActorKeycloakSub(),
                event.getAction(),
                event.getResourceType(),
                event.getResourceId(),
                event.getCorrelationId(),
                event.getEventHash(),
                event.getPreviousHash(),
                event.getCreatedAt()
        );
    }
}
