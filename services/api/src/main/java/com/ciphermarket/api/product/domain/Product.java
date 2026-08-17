package com.ciphermarket.api.product.domain;

import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.common.enums.ProductType;
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
@Table(name = "products")
public class Product {

    @Id
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "price_cents", nullable = false)
    private long priceCents;

    @Column(nullable = false)
    private String currency = "USD";

    @Column(name = "licence_type")
    private String licenceType;

    @Column(name = "usage_terms", columnDefinition = "TEXT")
    private String usageTerms;

    @Column(name = "refund_policy", columnDefinition = "TEXT")
    private String refundPolicy;

    @Column(name = "current_version_id")
    private UUID currentVersionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Product() {
    }

    public Product(
            UUID organisationId,
            UUID categoryId,
            String name,
            String slug,
            ProductType productType,
            long priceCents,
            String currency
    ) {
        this.id = UUID.randomUUID();
        this.organisationId = organisationId;
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.productType = productType;
        this.priceCents = priceCents;
        this.currency = currency != null ? currency : "USD";
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

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public ProductType getProductType() {
        return productType;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public long getPriceCents() {
        return priceCents;
    }

    public String getCurrency() {
        return currency;
    }

    public String getLicenceType() {
        return licenceType;
    }

    public String getUsageTerms() {
        return usageTerms;
    }

    public String getRefundPolicy() {
        return refundPolicy;
    }

    public UUID getCurrentVersionId() {
        return currentVersionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getEntityVersion() {
        return version;
    }

    public void updateDetails(
            String name,
            String shortDescription,
            String fullDescription,
            UUID categoryId,
            long priceCents,
            String currency,
            String licenceType,
            String usageTerms,
            String refundPolicy
    ) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.categoryId = categoryId;
        if (priceCents >= 0) {
            this.priceCents = priceCents;
        }
        if (currency != null && !currency.isBlank()) {
            this.currency = currency;
        }
        this.licenceType = licenceType;
        this.usageTerms = usageTerms;
        this.refundPolicy = refundPolicy;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public void setCurrentVersionId(UUID currentVersionId) {
        this.currentVersionId = currentVersionId;
    }
}
