package com.ciphermarket.api.commerce.dto;

import com.ciphermarket.api.common.enums.ProductType;
import com.ciphermarket.api.product.domain.Product;

import java.time.Instant;
import java.util.UUID;

public record CatalogueProductResponse(
        UUID id,
        UUID categoryId,
        String name,
        String slug,
        String shortDescription,
        ProductType productType,
        long priceCents,
        String currency,
        String licenceType,
        Instant publishedAt
) {
    public static CatalogueProductResponse from(Product product) {
        return new CatalogueProductResponse(
                product.getId(),
                product.getCategoryId(),
                product.getName(),
                product.getSlug(),
                product.getShortDescription(),
                product.getProductType(),
                product.getPriceCents(),
                product.getCurrency(),
                product.getLicenceType(),
                product.getUpdatedAt()
        );
    }
}
