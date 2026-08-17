package com.ciphermarket.api.product.dto;

import com.ciphermarket.api.common.enums.ProductVersionStatus;
import com.ciphermarket.api.product.domain.ProductVersion;

import java.time.Instant;
import java.util.UUID;

public record ProductVersionResponse(
        UUID id,
        UUID productId,
        String versionLabel,
        String changelog,
        ProductVersionStatus status,
        Instant publishedAt,
        Instant createdAt
) {
    public static ProductVersionResponse from(ProductVersion version) {
        return new ProductVersionResponse(
                version.getId(),
                version.getProductId(),
                version.getVersionLabel(),
                version.getChangelog(),
                version.getStatus(),
                version.getPublishedAt(),
                version.getCreatedAt()
        );
    }
}
