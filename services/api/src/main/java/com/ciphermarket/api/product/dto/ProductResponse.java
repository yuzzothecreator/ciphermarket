package com.ciphermarket.api.product.dto;

import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.common.enums.ProductType;
import com.ciphermarket.api.product.domain.Product;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID organisationId,
        UUID categoryId,
        String name,
        String slug,
        String shortDescription,
        String fullDescription,
        ProductType productType,
        ProductStatus status,
        long priceCents,
        String currency,
        String licenceType,
        UUID currentVersionId,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getOrganisationId(),
                product.getCategoryId(),
                product.getName(),
                product.getSlug(),
                product.getShortDescription(),
                product.getFullDescription(),
                product.getProductType(),
                product.getStatus(),
                product.getPriceCents(),
                product.getCurrency(),
                product.getLicenceType(),
                product.getCurrentVersionId(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
