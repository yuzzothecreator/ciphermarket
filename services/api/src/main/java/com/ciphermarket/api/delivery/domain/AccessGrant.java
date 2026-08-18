package com.ciphermarket.api.delivery.domain;

import com.ciphermarket.api.common.enums.AccessGrantStatus;
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
@Table(name = "access_grants")
public class AccessGrant {

    @Id
    private UUID id;

    @Column(name = "licence_id", nullable = false)
    private UUID licenceId;

    @Column(name = "buyer_user_id", nullable = false)
    private UUID buyerUserId;

    @Column(name = "product_asset_id", nullable = false)
    private UUID productAssetId;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessGrantStatus status = AccessGrantStatus.ACTIVE;

    @Column(name = "max_uses", nullable = false)
    private int maxUses;

    @Column(name = "use_count", nullable = false)
    private int useCount;

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

    protected AccessGrant() {
    }

    public AccessGrant(
            UUID licenceId,
            UUID buyerUserId,
            UUID productAssetId,
            UUID deviceId,
            String tokenHash,
            int maxUses,
            Instant expiresAt
    ) {
        this.id = UUID.randomUUID();
        this.licenceId = licenceId;
        this.buyerUserId = buyerUserId;
        this.productAssetId = productAssetId;
        this.deviceId = deviceId;
        this.tokenHash = tokenHash;
        this.maxUses = maxUses;
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

    public UUID getLicenceId() {
        return licenceId;
    }

    public UUID getBuyerUserId() {
        return buyerUserId;
    }

    public UUID getProductAssetId() {
        return productAssetId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public AccessGrantStatus getStatus() {
        return status;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getUseCount() {
        return useCount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsable() {
        return status == AccessGrantStatus.ACTIVE
                && Instant.now().isBefore(expiresAt)
                && useCount < maxUses;
    }

    public void recordUse() {
        this.useCount++;
        if (useCount >= maxUses) {
            this.status = AccessGrantStatus.EXHAUSTED;
        }
    }

    public void revoke() {
        this.status = AccessGrantStatus.REVOKED;
        this.revokedAt = Instant.now();
    }
}
