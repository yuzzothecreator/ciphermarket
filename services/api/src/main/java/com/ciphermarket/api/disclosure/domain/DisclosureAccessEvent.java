package com.ciphermarket.api.disclosure.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "disclosure_access_events")
public class DisclosureAccessEvent {

    @Id
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "actor_user_id", nullable = false)
    private UUID actorUserId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DisclosureAccessEvent() {
    }

    public DisclosureAccessEvent(
            UUID organisationId,
            UUID requestId,
            UUID documentId,
            UUID actorUserId,
            String eventType,
            Map<String, Object> details
    ) {
        this.id = UUID.randomUUID();
        this.organisationId = organisationId;
        this.requestId = requestId;
        this.documentId = documentId;
        this.actorUserId = actorUserId;
        this.eventType = eventType;
        this.details = details;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public String getEventType() {
        return eventType;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
