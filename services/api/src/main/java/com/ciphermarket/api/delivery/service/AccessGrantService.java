package com.ciphermarket.api.delivery.service;

import com.ciphermarket.api.common.enums.AssetStatus;
import com.ciphermarket.api.common.exception.AccessDeniedException;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.config.DeliveryProperties;
import com.ciphermarket.api.delivery.domain.AccessGrant;
import com.ciphermarket.api.delivery.domain.Licence;
import com.ciphermarket.api.delivery.domain.RegisteredDevice;
import com.ciphermarket.api.delivery.dto.AccessGrantResponse;
import com.ciphermarket.api.delivery.dto.CreateAccessGrantRequest;
import com.ciphermarket.api.delivery.repository.AccessGrantRepository;
import com.ciphermarket.api.delivery.repository.LicenceRepository;
import com.ciphermarket.api.delivery.signing.LicenceSigningService;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.product.domain.ProductAsset;
import com.ciphermarket.api.product.repository.ProductAssetRepository;
import com.ciphermarket.api.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AccessGrantService {

    private final AccessGrantRepository accessGrantRepository;
    private final LicenceRepository licenceRepository;
    private final ProductAssetRepository assetRepository;
    private final UserProfileService userProfileService;
    private final DeviceRegistrationService deviceRegistrationService;
    private final LicenceSigningService signingService;
    private final DeliveryProperties deliveryProperties;

    public AccessGrantService(
            AccessGrantRepository accessGrantRepository,
            LicenceRepository licenceRepository,
            ProductAssetRepository assetRepository,
            UserProfileService userProfileService,
            DeviceRegistrationService deviceRegistrationService,
            LicenceSigningService signingService,
            DeliveryProperties deliveryProperties
    ) {
        this.accessGrantRepository = accessGrantRepository;
        this.licenceRepository = licenceRepository;
        this.assetRepository = assetRepository;
        this.userProfileService = userProfileService;
        this.deviceRegistrationService = deviceRegistrationService;
        this.signingService = signingService;
        this.deliveryProperties = deliveryProperties;
    }

    @Transactional
    public AccessGrantResponse createGrant(AuthenticatedUser user, UUID entitlementId, CreateAccessGrantRequest request) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        Licence licence = licenceRepository.findByEntitlementId(entitlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Licence not found — issue licence first"));

        if (!licence.getBuyerUserId().equals(profile.getId())) {
            throw new AccessDeniedException("Licence does not belong to current user");
        }
        if (!licence.isValid()) {
            throw new AccessDeniedException("Licence is expired or revoked");
        }

        UUID deviceId = null;
        if (deliveryProperties.requireDeviceRegistration()) {
            if (request.deviceId() == null) {
                throw new IllegalArgumentException("Device registration required");
            }
            RegisteredDevice device = deviceRegistrationService.requireActiveDevice(profile.getId(), request.deviceId());
            device.markSeen();
            deviceId = device.getId();
        } else if (request.deviceId() != null) {
            RegisteredDevice device = deviceRegistrationService.requireActiveDevice(profile.getId(), request.deviceId());
            deviceId = device.getId();
        }

        ProductAsset asset = assetRepository.findByProductVersionId(licence.getProductVersionId()).stream()
                .filter(a -> a.getStatus() == AssetStatus.READY)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Deliverable asset not found"));

        String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = signingService.hashFingerprint(rawToken);
        Instant expiresAt = Instant.now().plus(deliveryProperties.grantTtlMinutes(), ChronoUnit.MINUTES);

        AccessGrant grant = accessGrantRepository.save(new AccessGrant(
                licence.getId(),
                profile.getId(),
                asset.getId(),
                deviceId,
                tokenHash,
                deliveryProperties.maxUsesPerGrant(),
                expiresAt
        ));

        return new AccessGrantResponse(grant.getId(), rawToken, expiresAt, grant.getMaxUses(), grant.getUseCount());
    }

    @Transactional(readOnly = true)
    public AccessGrant requireUsableGrant(String rawToken) {
        String tokenHash = signingService.hashFingerprint(rawToken);
        AccessGrant grant = accessGrantRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AccessDeniedException("Invalid access grant"));
        if (!grant.isUsable()) {
            throw new AccessDeniedException("Access grant expired or exhausted");
        }
        Licence licence = licenceRepository.findById(grant.getLicenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Licence not found"));
        if (!licence.isValid()) {
            throw new AccessDeniedException("Licence is no longer valid");
        }
        return grant;
    }

    @Transactional
    public void recordUse(AccessGrant grant) {
        grant.recordUse();
        accessGrantRepository.save(grant);
    }
}
