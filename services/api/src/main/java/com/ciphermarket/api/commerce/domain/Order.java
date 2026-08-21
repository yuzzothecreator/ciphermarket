package com.ciphermarket.api.commerce.domain;

import com.ciphermarket.api.common.enums.OrderStatus;
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
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(name = "buyer_user_id", nullable = false)
    private UUID buyerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(name = "subtotal_cents", nullable = false)
    private long subtotalCents;

    @Column(nullable = false)
    private String currency;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Order() {
    }

    public Order(UUID buyerUserId, long subtotalCents, String currency) {
        this.id = UUID.randomUUID();
        this.buyerUserId = buyerUserId;
        this.subtotalCents = subtotalCents;
        this.currency = currency;
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

    public UUID getBuyerUserId() {
        return buyerUserId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public long getSubtotalCents() {
        return subtotalCents;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void markFailed() {
        this.status = OrderStatus.FAILED;
    }

    public void markCancelled() {
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public void markRefunded() {
        this.status = OrderStatus.REFUNDED;
    }
}
