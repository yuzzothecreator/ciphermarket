package com.ciphermarket.api.securityops.dto;

import com.ciphermarket.api.common.enums.SecurityEventSeverity;
import com.ciphermarket.api.common.enums.SecurityEventStatus;
import com.ciphermarket.api.securityops.domain.SecurityEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SecurityEventResponse(
        UUID id,
        UUID organisationId,
        UUID actorUserId,
        String eventType,
        SecurityEventSeverity severity,
        SecurityEventStatus status,
        String resourceType,
        UUID resourceId,
        String summary,
        Map<String, Object> details,
        String correlationId,
        Instant createdAt
) {
    public static SecurityEventResponse from(SecurityEvent event) {
        return new SecurityEventResponse(
                event.getId(),
                event.getOrganisationId(),
                event.getActorUserId(),
                event.getEventType(),
                event.getSeverity(),
                event.getStatus(),
                event.getResourceType(),
                event.getResourceId(),
                event.getSummary(),
                event.getDetails(),
                event.getCorrelationId(),
                event.getCreatedAt()
        );
    }
}
