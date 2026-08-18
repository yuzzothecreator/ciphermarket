package com.ciphermarket.api.securityops.domain;

import com.ciphermarket.api.common.enums.ApprovalActionType;
import com.ciphermarket.api.common.enums.ApprovalStatus;
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
@Table(name = "approval_requests")
public class ApprovalRequest {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ApprovalActionType actionType;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "organisation_id")
    private UUID organisationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Column(name = "decided_by")
    private UUID decidedBy;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected ApprovalRequest() {
    }

    public ApprovalRequest(
            ApprovalActionType actionType,
            String resourceType,
            UUID resourceId,
            UUID organisationId,
            Map<String, Object> payload,
            String reason,
            UUID requestedBy
    ) {
        this.id = UUID.randomUUID();
        this.actionType = actionType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.organisationId = organisationId;
        this.payload = payload;
        this.reason = reason;
        this.requestedBy = requestedBy;
        this.requestedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (requestedAt == null) {
            requestedAt = now;
        }
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

    public ApprovalActionType getActionType() {
        return actionType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getReason() {
        return reason;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public UUID getRequestedBy() {
        return requestedBy;
    }

    public UUID getDecidedBy() {
        return decidedBy;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void decide(ApprovalStatus decision, UUID decidedBy, String decisionReason) {
        this.status = decision;
        this.decidedBy = decidedBy;
        this.decisionReason = decisionReason;
        this.decidedAt = Instant.now();
    }
}
