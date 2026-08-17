package com.ciphermarket.api.product.api;

import com.ciphermarket.api.product.dto.CreateProductRequest;
import com.ciphermarket.api.product.dto.CreateProductVersionRequest;
import com.ciphermarket.api.product.dto.ProductResponse;
import com.ciphermarket.api.product.dto.ProductVersionResponse;
import com.ciphermarket.api.product.dto.UpdateProductRequest;
import com.ciphermarket.api.product.service.ProductService;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/products")
@Tag(name = "Products", description = "Creator product management")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create a product")
    public ProductResponse create(
            @PathVariable UUID organisationId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateProductRequest request
    ) {
        return productService.createProduct(organisationId, user, request);
    }

    @GetMapping
    @Operation(summary = "List organisation products")
    public List<ProductResponse> list(
            @PathVariable UUID organisationId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return productService.listProducts(organisationId, user);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    public ProductResponse get(
            @PathVariable UUID organisationId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return productService.getProduct(organisationId, productId, user);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update product details")
    public ProductResponse update(
            @PathVariable UUID organisationId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.updateProduct(organisationId, productId, user, request);
    }

    @PostMapping("/{productId}/versions")
    @Operation(summary = "Create a product version")
    public ProductVersionResponse createVersion(
            @PathVariable UUID organisationId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateProductVersionRequest request
    ) {
        return productService.createVersion(organisationId, productId, user, request);
    }

    @GetMapping("/{productId}/versions")
    @Operation(summary = "List product versions")
    public List<ProductVersionResponse> listVersions(
            @PathVariable UUID organisationId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return productService.listVersions(organisationId, productId, user);
    }

    @PostMapping("/{productId}/submit-review")
    @Operation(summary = "Submit product for platform review")
    public ProductResponse submitForReview(
            @PathVariable UUID organisationId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return productService.submitForReview(organisationId, productId, user);
    }

    @PostMapping("/{productId}/versions/{versionId}/publish")
    @Operation(summary = "Publish a product version")
    public ProductResponse publish(
            @PathVariable UUID organisationId,
            @PathVariable UUID productId,
            @PathVariable UUID versionId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return productService.publishProduct(organisationId, productId, versionId, user);
    }
}
