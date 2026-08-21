package com.ciphermarket.api.commerce.dto;

import com.ciphermarket.api.common.enums.ProductType;
import com.ciphermarket.api.product.domain.Product;

import java.util.UUID;

public record CatalogueProductDetailResponse(
        UUID id,
        UUID organisationId,
        String organisationSlug,
        UUID categoryId,
        String name,
        String slug,
        String shortDescription,
        String fullDescription,
        ProductType productType,
        long priceCents,
        String currency,
        String licenceType,
        String usageTerms,
        String refundPolicy,
        String coverImageUrl
) {
    public static CatalogueProductDetailResponse from(Product product, String organisationSlug) {
        return new CatalogueProductDetailResponse(
                product.getId(),
                product.getOrganisationId(),
                organisationSlug,
                product.getCategoryId(),
                product.getName(),
                product.getSlug(),
                product.getShortDescription(),
                product.getFullDescription(),
                product.getProductType(),
                product.getPriceCents(),
                product.getCurrency(),
                product.getLicenceType(),
                product.getUsageTerms(),
                product.getRefundPolicy(),
                product.getCoverImageUrl()
        );
    }
}
