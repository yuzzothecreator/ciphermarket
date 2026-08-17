package com.ciphermarket.api.product.domain;

import com.ciphermarket.api.common.enums.ProductVersionStatus;
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
@Table(name = "product_versions")
public class ProductVersion {

    @Id
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "version_label", nullable = false)
    private String versionLabel;

    @Column(columnDefinition = "TEXT")
    private String changelog;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductVersionStatus status = ProductVersionStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected ProductVersion() {
    }

    public ProductVersion(UUID organisationId, UUID productId, String versionLabel, String changelog) {
        this.id = UUID.randomUUID();
        this.organisationId = organisationId;
        this.productId = productId;
        this.versionLabel = versionLabel;
        this.changelog = changelog;
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

    public String getVersionLabel() {
        return versionLabel;
    }

    public String getChangelog() {
        return changelog;
    }

    public ProductVersionStatus getStatus() {
        return status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setStatus(ProductVersionStatus status) {
        this.status = status;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
