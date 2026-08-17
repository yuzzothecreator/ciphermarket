package com.ciphermarket.api.product.domain;

import com.ciphermarket.api.common.enums.UploadSessionStatus;
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
@Table(name = "upload_sessions")
public class UploadSession {

    @Id
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_version_id", nullable = false)
    private UUID productVersionId;

    @Column(name = "asset_id")
    private UUID assetId;

    @Column(name = "initiated_by_user_id", nullable = false)
    private UUID initiatedByUserId;

    @Column(name = "declared_content_type", nullable = false)
    private String declaredContentType;

    @Column(name = "declared_file_name", nullable = false)
    private String declaredFileName;

    @Column(name = "max_size_bytes", nullable = false)
    private long maxSizeBytes;

    @Column(name = "quarantine_object_key", nullable = false)
    private String quarantineObjectKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadSessionStatus status = UploadSessionStatus.INITIATED;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected UploadSession() {
    }

    public UploadSession(
            UUID organisationId,
            UUID productId,
            UUID productVersionId,
            UUID assetId,
            UUID initiatedByUserId,
            String declaredContentType,
            String declaredFileName,
            long maxSizeBytes,
            String quarantineObjectKey,
            Instant expiresAt
    ) {
        this.id = UUID.randomUUID();
        this.organisationId = organisationId;
        this.productId = productId;
        this.productVersionId = productVersionId;
        this.assetId = assetId;
        this.initiatedByUserId = initiatedByUserId;
        this.declaredContentType = declaredContentType;
        this.declaredFileName = declaredFileName;
        this.maxSizeBytes = maxSizeBytes;
        this.quarantineObjectKey = quarantineObjectKey;
        this.expiresAt = expiresAt;
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

    public UUID getProductId() {
        return productId;
    }

    public UUID getProductVersionId() {
        return productVersionId;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public String getDeclaredContentType() {
        return declaredContentType;
    }

    public String getDeclaredFileName() {
        return declaredFileName;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public String getQuarantineObjectKey() {
        return quarantineObjectKey;
    }

    public UploadSessionStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setStatus(UploadSessionStatus status) {
        this.status = status;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
