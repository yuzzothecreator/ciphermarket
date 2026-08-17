package com.ciphermarket.api.commerce.api;

import com.ciphermarket.api.commerce.dto.CatalogueProductDetailResponse;
import com.ciphermarket.api.commerce.dto.CatalogueProductResponse;
import com.ciphermarket.api.commerce.service.CatalogueService;
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
@RequestMapping("/api/v1/catalogue/products")
@Tag(name = "Catalogue", description = "Public product catalogue")
public class CatalogueController {

    private final CatalogueService catalogueService;

    public CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping
    @Operation(summary = "List published products")
    public List<CatalogueProductResponse> list(@RequestParam(required = false) UUID categoryId) {
        return catalogueService.listPublished(categoryId);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get published product details")
    public CatalogueProductDetailResponse get(@PathVariable UUID productId) {
        return catalogueService.getPublishedProduct(productId);
    }
}
