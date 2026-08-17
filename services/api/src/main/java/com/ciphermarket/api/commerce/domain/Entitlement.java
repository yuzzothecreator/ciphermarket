package com.ciphermarket.api.commerce.domain;

import com.ciphermarket.api.common.enums.EntitlementStatus;
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
@Table(name = "entitlements")
public class Entitlement {

    @Id
    private UUID id;

    @Column(name = "buyer_user_id", nullable = false)
    private UUID buyerUserId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntitlementStatus status = EntitlementStatus.ACTIVE;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Entitlement() {
    }

    public Entitlement(UUID buyerUserId, UUID productId, UUID orderId, UUID orderItemId) {
        this.id = UUID.randomUUID();
        this.buyerUserId = buyerUserId;
        this.productId = productId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.grantedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (grantedAt == null) {
            grantedAt = now;
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

    public UUID getBuyerUserId() {
        return buyerUserId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getOrderItemId() {
        return orderItemId;
    }

    public EntitlementStatus getStatus() {
        return status;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }
}
