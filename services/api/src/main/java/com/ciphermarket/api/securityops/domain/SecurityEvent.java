package com.ciphermarket.api.securityops.domain;

import com.ciphermarket.api.common.enums.SecurityEventSeverity;
import com.ciphermarket.api.common.enums.SecurityEventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    private UUID id;

    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SecurityEventSeverity severity = SecurityEventSeverity.INFO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SecurityEventStatus status = SecurityEventStatus.OPEN;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected SecurityEvent() {
    }

    public SecurityEvent(
            UUID organisationId,
            UUID actorUserId,
            String eventType,
            SecurityEventSeverity severity,
            String resourceType,
            UUID resourceId,
            String summary,
            Map<String, Object> details,
            String correlationId
    ) {
        this.id = UUID.randomUUID();
        this.organisationId = organisationId;
        this.actorUserId = actorUserId;
        this.eventType = eventType;
        this.severity = severity;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.summary = summary;
        this.details = details;
        this.correlationId = correlationId;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public String getEventType() {
        return eventType;
    }

    public SecurityEventSeverity getSeverity() {
        return severity;
    }

    public SecurityEventStatus getStatus() {
        return status;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public String getSummary() {
        return summary;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void acknowledge(UUID actorId) {
        this.status = SecurityEventStatus.ACKNOWLEDGED;
        this.acknowledgedBy = actorId;
        this.acknowledgedAt = Instant.now();
    }

    public void close() {
        this.status = SecurityEventStatus.CLOSED;
    }
}
