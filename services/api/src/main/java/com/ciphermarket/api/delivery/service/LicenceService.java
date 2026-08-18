package com.ciphermarket.api.delivery.service;

import com.ciphermarket.api.commerce.domain.Entitlement;
import com.ciphermarket.api.commerce.repository.EntitlementRepository;
import com.ciphermarket.api.common.enums.AssetStatus;
import com.ciphermarket.api.common.enums.EntitlementStatus;
import com.ciphermarket.api.common.exception.AccessDeniedException;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.config.DeliveryProperties;
import com.ciphermarket.api.delivery.domain.Licence;
import com.ciphermarket.api.delivery.dto.LicenceResponse;
import com.ciphermarket.api.delivery.repository.LicenceRepository;
import com.ciphermarket.api.delivery.signing.LicenceSigningService;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.repository.ProductAssetRepository;
import com.ciphermarket.api.product.repository.ProductRepository;
import com.ciphermarket.api.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class LicenceService {

    private final LicenceRepository licenceRepository;
    private final EntitlementRepository entitlementRepository;
    private final ProductRepository productRepository;
    private final ProductAssetRepository assetRepository;
    private final UserProfileService userProfileService;
    private final LicenceSigningService signingService;
    private final DeliveryProperties deliveryProperties;

    public LicenceService(
            LicenceRepository licenceRepository,
            EntitlementRepository entitlementRepository,
            ProductRepository productRepository,
            ProductAssetRepository assetRepository,
            UserProfileService userProfileService,
            LicenceSigningService signingService,
            DeliveryProperties deliveryProperties
    ) {
        this.licenceRepository = licenceRepository;
        this.entitlementRepository = entitlementRepository;
        this.productRepository = productRepository;
        this.assetRepository = assetRepository;
        this.userProfileService = userProfileService;
        this.signingService = signingService;
        this.deliveryProperties = deliveryProperties;
    }

    @Transactional
    public Licence issueForEntitlement(Entitlement entitlement) {
        return licenceRepository.findByEntitlementId(entitlement.getId())
                .orElseGet(() -> createLicence(entitlement));
    }

    @Transactional(readOnly = true)
    public LicenceResponse getForEntitlement(AuthenticatedUser user, UUID entitlementId) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        Entitlement entitlement = requireActiveEntitlement(entitlementId, profile.getId());
        Licence licence = licenceRepository.findByEntitlementId(entitlement.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Licence not yet issued"));
        return LicenceResponse.from(licence);
    }

    @Transactional
    public LicenceResponse issueIfMissing(AuthenticatedUser user, UUID entitlementId) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        Entitlement entitlement = requireActiveEntitlement(entitlementId, profile.getId());
        Licence licence = issueForEntitlement(entitlement);
        return LicenceResponse.from(licence);
    }

    private Licence createLicence(Entitlement entitlement) {
        Product product = productRepository.findById(entitlement.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        UUID versionId = entitlement.getProductVersionId() != null
                ? entitlement.getProductVersionId()
                : product.getCurrentVersionId();
        if (versionId == null) {
            throw new IllegalStateException("Product has no published version");
        }

        Instant expiresAt = Instant.now().plus(deliveryProperties.licenceValidityDays(), ChronoUnit.DAYS);
        UUID licenceId = UUID.randomUUID();
        LicenceSigningService.SignedLicencePayload signed = signingService.sign(
                new LicenceSigningService.LicenceClaims(
                        licenceId,
                        entitlement.getId(),
                        entitlement.getBuyerUserId(),
                        entitlement.getProductId(),
                        versionId,
                        Instant.now().getEpochSecond(),
                        expiresAt.getEpochSecond()
                )
        );

        return licenceRepository.save(new Licence(
                licenceId,
                entitlement.getId(),
                entitlement.getBuyerUserId(),
                entitlement.getProductId(),
                versionId,
                signed.payload(),
                signed.signature(),
                expiresAt
        ));
    }

    private Entitlement requireActiveEntitlement(UUID entitlementId, UUID buyerUserId) {
        Entitlement entitlement = entitlementRepository.findByIdAndBuyerUserId(entitlementId, buyerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Entitlement not found"));
        if (entitlement.getStatus() != EntitlementStatus.ACTIVE) {
            throw new AccessDeniedException("Entitlement is not active");
        }
        UUID versionId = entitlement.getProductVersionId();
        if (versionId == null) {
            Product product = productRepository.findById(entitlement.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            versionId = product.getCurrentVersionId();
        }
        if (versionId == null || assetRepository.findByProductVersionId(versionId).stream()
                .noneMatch(a -> a.getStatus() == AssetStatus.READY)) {
            throw new IllegalStateException("No deliverable asset for this entitlement");
        }
        return entitlement;
    }
}
