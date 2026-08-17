package com.ciphermarket.api.commerce.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_slug", nullable = false)
    private String productSlug;

    @Column(name = "unit_price_cents", nullable = false)
    private long unitPriceCents;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_total_cents", nullable = false)
    private long lineTotalCents;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrderItem() {
    }

    public OrderItem(
            UUID orderId,
            UUID productId,
            UUID organisationId,
            String productName,
            String productSlug,
            long unitPriceCents,
            String currency,
            int quantity
    ) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.productId = productId;
        this.organisationId = organisationId;
        this.productName = productName;
        this.productSlug = productSlug;
        this.unitPriceCents = unitPriceCents;
        this.currency = currency;
        this.quantity = quantity;
        this.lineTotalCents = unitPriceCents * quantity;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductSlug() {
        return productSlug;
    }

    public long getUnitPriceCents() {
        return unitPriceCents;
    }

    public String getCurrency() {
        return currency;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getLineTotalCents() {
        return lineTotalCents;
    }
}
