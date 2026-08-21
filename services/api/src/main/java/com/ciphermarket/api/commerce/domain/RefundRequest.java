package com.ciphermarket.api.commerce.domain;

import com.ciphermarket.api.common.enums.RefundRequestStatus;
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
@Table(name = "refund_requests")
public class RefundRequest {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "buyer_user_id", nullable = false)
    private UUID buyerUserId;

    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundRequestStatus status = RefundRequestStatus.REQUESTED;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "approval_request_id")
    private UUID approvalRequestId;

    @Column(name = "provider_refund_ref")
    private String providerRefundRef;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected RefundRequest() {
    }

    public RefundRequest(
            UUID orderId,
            UUID paymentId,
            UUID buyerUserId,
            UUID organisationId,
            long amountCents,
            String currency,
            String reason
    ) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.buyerUserId = buyerUserId;
        this.organisationId = organisationId;
        this.amountCents = amountCents;
        this.currency = currency;
        this.reason = reason;
        this.requestedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (requestedAt == null) {
            requestedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getBuyerUserId() {
        return buyerUserId;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public String getCurrency() {
        return currency;
    }

    public String getReason() {
        return reason;
    }

    public RefundRequestStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public UUID getApprovalRequestId() {
        return approvalRequestId;
    }

    public String getProviderRefundRef() {
        return providerRefundRef;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markUnderReview(UUID approvalRequestId) {
        this.status = RefundRequestStatus.UNDER_REVIEW;
        this.approvalRequestId = approvalRequestId;
    }

    public void markRejected(String reason) {
        this.status = RefundRequestStatus.REJECTED;
        this.rejectionReason = reason;
        this.decidedAt = Instant.now();
    }

    public void markApproved() {
        this.status = RefundRequestStatus.APPROVED;
        this.decidedAt = Instant.now();
    }

    public void markCompleted(String providerRefundRef) {
        this.status = RefundRequestStatus.COMPLETED;
        this.providerRefundRef = providerRefundRef;
        this.completedAt = Instant.now();
    }

    public void markCancelled() {
        this.status = RefundRequestStatus.CANCELLED;
        this.decidedAt = Instant.now();
    }
}
