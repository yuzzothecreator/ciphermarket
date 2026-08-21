package com.ciphermarket.api.commerce.api;

import com.ciphermarket.api.commerce.dto.CatalogueProductDetailResponse;
import com.ciphermarket.api.commerce.dto.CatalogueProductResponse;
import com.ciphermarket.api.commerce.dto.CatalogueSort;
import com.ciphermarket.api.commerce.dto.CreatorStorefrontResponse;
import com.ciphermarket.api.commerce.service.CatalogueService;
import com.ciphermarket.api.common.enums.ProductType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalogue")
@Tag(name = "Catalogue", description = "Public product catalogue and creator storefronts")
public class CatalogueController {

    private final CatalogueService catalogueService;

    public CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping("/products")
    @Operation(summary = "Search and list published products")
    public List<CatalogueProductResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID organisationId,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) Long minPriceCents,
            @RequestParam(required = false) Long maxPriceCents,
            @RequestParam(required = false, defaultValue = "NEWEST") CatalogueSort sort
    ) {
        return catalogueService.search(
                q, categoryId, organisationId, productType, minPriceCents, maxPriceCents, sort
        );
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get published product details")
    public CatalogueProductDetailResponse get(@PathVariable UUID productId) {
        return catalogueService.getPublishedProduct(productId);
    }

    @GetMapping("/creators/{slug}")
    @Operation(summary = "Get a creator storefront by organisation slug")
    public CreatorStorefrontResponse storefront(@PathVariable String slug) {
        return catalogueService.getStorefront(slug);
    }
}
