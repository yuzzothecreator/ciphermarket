package com.ciphermarket.api.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    private UUID id;

    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_keycloak_sub")
    private String actorKeycloakSub;

    @Column(nullable = false)
    private String action;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_summary", columnDefinition = "jsonb")
    private Map<String, Object> beforeSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_summary", columnDefinition = "jsonb")
    private Map<String, Object> afterSummary;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "approval_actor_id")
    private UUID approvalActorId;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "event_hash")
    private String eventHash;

    @Column(name = "previous_hash")
    private String previousHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AuditEvent() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public static AuditEvent create(
            UUID id,
            UUID organisationId,
            UUID actorUserId,
            String actorKeycloakSub,
            String action,
            String resourceType,
            UUID resourceId,
            Map<String, Object> beforeSummary,
            Map<String, Object> afterSummary,
            String correlationId,
            String eventHash,
            String previousHash
    ) {
        AuditEvent event = new AuditEvent();
        event.id = id;
        event.organisationId = organisationId;
        event.actorUserId = actorUserId;
        event.actorKeycloakSub = actorKeycloakSub;
        event.action = action;
        event.resourceType = resourceType;
        event.resourceId = resourceId;
        event.beforeSummary = beforeSummary;
        event.afterSummary = afterSummary;
        event.correlationId = correlationId;
        event.eventHash = eventHash;
        event.previousHash = previousHash;
        return event;
    }

    public UUID getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getEventHash() {
        return eventHash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public String getActorKeycloakSub() {
        return actorKeycloakSub;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
