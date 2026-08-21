package com.ciphermarket.api.commerce.dto;

import java.util.List;

public record CreatorStorefrontResponse(
        CreatorStorefrontOrganisation organisation,
        List<CatalogueProductResponse> products
) {
    public record CreatorStorefrontOrganisation(
            java.util.UUID id,
            String name,
            String slug,
            String description
    ) {
    }
}
