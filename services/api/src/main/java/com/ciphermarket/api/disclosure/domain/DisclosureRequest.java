package com.ciphermarket.api.disclosure.domain;

import com.ciphermarket.api.common.enums.DisclosureRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "disclosure_requests")
public class DisclosureRequest {

    @Id
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "confidentiality_terms", nullable = false, columnDefinition = "TEXT")
    private String confidentialityTerms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisclosureRequestStatus status = DisclosureRequestStatus.PENDING;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "disclosed_at", nullable = false)
    private Instant disclosedAt;

    @Column(name = "decision_note", columnDefinition = "TEXT")
    private String decisionNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected DisclosureRequest() {
    }

    public DisclosureRequest(
            UUID organisationId,
            UUID documentId,
            UUID createdByUserId,
            UUID recipientUserId,
            String recipientEmail,
            String confidentialityTerms,
            Instant expiresAt
    ) {
        this.id = UUID.randomUUID();
        this.organisationId = organisationId;
        this.documentId = documentId;
        this.createdByUserId = createdByUserId;
        this.recipientUserId = recipientUserId;
        this.recipientEmail = recipientEmail;
        this.confidentialityTerms = confidentialityTerms;
        this.expiresAt = expiresAt;
        this.disclosedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (disclosedAt == null) {
            disclosedAt = now;
        }
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

    public UUID getDocumentId() {
        return documentId;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getConfidentialityTerms() {
        return confidentialityTerms;
    }

    public DisclosureRequestStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Instant getDisclosedAt() {
        return disclosedAt;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void accept() {
        this.status = DisclosureRequestStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
    }

    public void reject(String note) {
        this.status = DisclosureRequestStatus.REJECTED;
        this.rejectedAt = Instant.now();
        this.decisionNote = note;
    }

    public void revoke() {
        this.status = DisclosureRequestStatus.REVOKED;
        this.revokedAt = Instant.now();
    }

    public void markExpired() {
        this.status = DisclosureRequestStatus.EXPIRED;
    }
}
