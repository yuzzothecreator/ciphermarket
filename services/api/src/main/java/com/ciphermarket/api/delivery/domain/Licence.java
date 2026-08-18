package com.ciphermarket.api.delivery.domain;

import com.ciphermarket.api.common.enums.LicenceStatus;
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
@Table(name = "licences")
public class Licence {

    @Id
    private UUID id;

    @Column(name = "entitlement_id", nullable = false, unique = true)
    private UUID entitlementId;

    @Column(name = "buyer_user_id", nullable = false)
    private UUID buyerUserId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_version_id", nullable = false)
    private UUID productVersionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenceStatus status = LicenceStatus.ACTIVE;

    @Column(name = "token_payload", nullable = false, columnDefinition = "TEXT")
    private String tokenPayload;

    @Column(name = "token_signature", nullable = false, columnDefinition = "TEXT")
    private String tokenSignature;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Licence() {
    }

    public Licence(
            UUID id,
            UUID entitlementId,
            UUID buyerUserId,
            UUID productId,
            UUID productVersionId,
            String tokenPayload,
            String tokenSignature,
            Instant expiresAt
    ) {
        this.id = id;
        this.entitlementId = entitlementId;
        this.buyerUserId = buyerUserId;
        this.productId = productId;
        this.productVersionId = productVersionId;
        this.tokenPayload = tokenPayload;
        this.tokenSignature = tokenSignature;
        this.issuedAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (issuedAt == null) {
            issuedAt = now;
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

    public UUID getEntitlementId() {
        return entitlementId;
    }

    public UUID getBuyerUserId() {
        return buyerUserId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getProductVersionId() {
        return productVersionId;
    }

    public LicenceStatus getStatus() {
        return status;
    }

    public String getTokenPayload() {
        return tokenPayload;
    }

    public String getTokenSignature() {
        return tokenSignature;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public String getSignedToken() {
        return tokenPayload + "." + tokenSignature;
    }

    public void revoke() {
        this.status = LicenceStatus.REVOKED;
        this.revokedAt = Instant.now();
    }

    public boolean isValid() {
        return status == LicenceStatus.ACTIVE && Instant.now().isBefore(expiresAt);
    }
}
