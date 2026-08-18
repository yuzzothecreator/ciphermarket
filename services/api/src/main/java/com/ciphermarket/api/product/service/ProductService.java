package com.ciphermarket.api.product.service;

import com.ciphermarket.api.audit.service.AuditService;
import com.ciphermarket.api.common.enums.OrganisationRole;
import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.common.enums.ProductVersionStatus;
import com.ciphermarket.api.common.exception.AccessDeniedException;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.organisation.service.OrganisationService;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.domain.ProductVersion;
import com.ciphermarket.api.product.dto.CreateProductRequest;
import com.ciphermarket.api.product.dto.CreateProductVersionRequest;
import com.ciphermarket.api.product.dto.ProductResponse;
import com.ciphermarket.api.product.dto.ProductVersionResponse;
import com.ciphermarket.api.product.dto.UpdateProductRequest;
import com.ciphermarket.api.product.repository.ProductAssetRepository;
import com.ciphermarket.api.product.repository.ProductRepository;
import com.ciphermarket.api.product.repository.ProductVersionRepository;
import com.ciphermarket.api.security.AuthenticatedUser;
import com.ciphermarket.api.securityops.service.SecurityEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVersionRepository versionRepository;
    private final ProductAssetRepository assetRepository;
    private final OrganisationService organisationService;
    private final UserProfileService userProfileService;
    private final AuditService auditService;
    private final SecurityEventService securityEventService;

    public ProductService(
            ProductRepository productRepository,
            ProductVersionRepository versionRepository,
            ProductAssetRepository assetRepository,
            OrganisationService organisationService,
            UserProfileService userProfileService,
            AuditService auditService,
            SecurityEventService securityEventService
    ) {
        this.productRepository = productRepository;
        this.versionRepository = versionRepository;
        this.assetRepository = assetRepository;
        this.organisationService = organisationService;
        this.userProfileService = userProfileService;
        this.auditService = auditService;
        this.securityEventService = securityEventService;
    }

    @Transactional
    public ProductResponse createProduct(UUID organisationId, AuthenticatedUser user, CreateProductRequest request) {
        requireProductManager(user, organisationId);
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());

        if (productRepository.existsByOrganisationIdAndSlug(organisationId, request.slug())) {
            throw new IllegalArgumentException("Product slug already exists in this organisation");
        }

        Product product = new Product(
                organisationId,
                request.categoryId(),
                request.name(),
                request.slug(),
                request.productType(),
                request.priceCents(),
                request.currency()
        );
        product.updateDetails(
                request.name(),
                request.shortDescription(),
                null,
                request.categoryId(),
                request.priceCents(),
                request.currency(),
                null,
                null,
                null
        );
        product = productRepository.save(product);

        auditService.record(
                organisationId,
                profile.getId(),
                user.keycloakSub(),
                "PRODUCT_CREATED",
                "product",
                product.getId(),
                null,
                Map.of("slug", product.getSlug(), "name", product.getName())
        );

        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts(UUID organisationId, AuthenticatedUser user) {
        organisationService.requireActiveMembership(user, organisationId);
        return productRepository.findByOrganisationIdOrderByUpdatedAtDesc(organisationId).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID organisationId, UUID productId, AuthenticatedUser user) {
        organisationService.requireActiveMembership(user, organisationId);
        return ProductResponse.from(requireProduct(organisationId, productId));
    }

    @Transactional
    public ProductResponse updateProduct(
            UUID organisationId,
            UUID productId,
            AuthenticatedUser user,
            UpdateProductRequest request
    ) {
        requireProductManager(user, organisationId);
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        Product product = requireProduct(organisationId, productId);

        product.updateDetails(
                request.name(),
                request.shortDescription(),
                request.fullDescription(),
                request.categoryId(),
                request.priceCents() != null ? request.priceCents() : product.getPriceCents(),
                request.currency(),
                request.licenceType(),
                request.usageTerms(),
                request.refundPolicy()
        );
        product = productRepository.save(product);

        auditService.record(
                organisationId,
                profile.getId(),
                user.keycloakSub(),
                "PRODUCT_UPDATED",
                "product",
                product.getId(),
                null,
                Map.of("name", product.getName())
        );

        return ProductResponse.from(product);
    }

    @Transactional
    public ProductVersionResponse createVersion(
            UUID organisationId,
            UUID productId,
            AuthenticatedUser user,
            CreateProductVersionRequest request
    ) {
        requireProductManager(user, organisationId);
        Product product = requireProduct(organisationId, productId);

        if (versionRepository.existsByProductIdAndVersionLabel(productId, request.versionLabel())) {
            throw new IllegalArgumentException("Version label already exists");
        }

        ProductVersion version = new ProductVersion(
                organisationId,
                productId,
                request.versionLabel(),
                request.changelog()
        );
        version = versionRepository.save(version);

        product.setStatus(ProductStatus.UPLOADING);
        productRepository.save(product);

        return ProductVersionResponse.from(version);
    }

    @Transactional(readOnly = true)
    public List<ProductVersionResponse> listVersions(UUID organisationId, UUID productId, AuthenticatedUser user) {
        organisationService.requireActiveMembership(user, organisationId);
        requireProduct(organisationId, productId);
        return versionRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(ProductVersionResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse submitForReview(UUID organisationId, UUID productId, AuthenticatedUser user) {
        requireProductManager(user, organisationId);
        Product product = requireProduct(organisationId, productId);

        boolean hasReadyAsset = versionRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .anyMatch(v -> assetRepository.findByProductVersionId(v.getId()).stream()
                        .anyMatch(a -> a.getStatus().name().equals("READY")));

        if (!hasReadyAsset) {
            throw new IllegalStateException("At least one processed asset is required before review submission");
        }

        product.setStatus(ProductStatus.UNDER_REVIEW);
        productRepository.save(product);

        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse publishProduct(UUID organisationId, UUID productId, UUID versionId, AuthenticatedUser user) {
        requireProductManager(user, organisationId);
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        Product product = requireProduct(organisationId, productId);
        ProductVersion version = versionRepository.findByIdAndOrganisationId(versionId, organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product version not found"));

        boolean versionReady = assetRepository.findByProductVersionId(versionId).stream()
                .anyMatch(a -> a.getStatus().name().equals("READY"));
        if (!versionReady) {
            throw new IllegalStateException("Version assets must be ready before publishing");
        }

        version.setStatus(ProductVersionStatus.PUBLISHED);
        version.setPublishedAt(Instant.now());
        versionRepository.save(version);

        product.setStatus(ProductStatus.PUBLISHED);
        product.setCurrentVersionId(versionId);
        product = productRepository.save(product);

        auditService.record(
                organisationId,
                profile.getId(),
                user.keycloakSub(),
                "PRODUCT_PUBLISHED",
                "product",
                product.getId(),
                null,
                Map.of("versionId", versionId.toString())
        );
        securityEventService.record(
                organisationId,
                profile.getId(),
                "PRODUCT_PUBLISHED",
                com.ciphermarket.api.common.enums.SecurityEventSeverity.LOW,
                "Product",
                product.getId(),
                "Product published to catalogue",
                Map.of("versionId", versionId.toString())
        );

        return ProductResponse.from(product);
    }

    public Product requireProduct(UUID organisationId, UUID productId) {
        return productRepository.findByIdAndOrganisationId(productId, organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public ProductVersion requireVersion(UUID organisationId, UUID versionId) {
        return versionRepository.findByIdAndOrganisationId(versionId, organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product version not found"));
    }

    private void requireProductManager(AuthenticatedUser user, UUID organisationId) {
        organisationService.requireOrganisationRole(user, organisationId, OrganisationRole.PRODUCT_MANAGER);
    }
}
