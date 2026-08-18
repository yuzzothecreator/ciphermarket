package com.ciphermarket.api.delivery.service;

import com.ciphermarket.api.common.enums.DeviceStatus;
import com.ciphermarket.api.common.exception.AccessDeniedException;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.delivery.domain.RegisteredDevice;
import com.ciphermarket.api.delivery.dto.DeviceResponse;
import com.ciphermarket.api.delivery.dto.RegisterDeviceRequest;
import com.ciphermarket.api.delivery.repository.RegisteredDeviceRepository;
import com.ciphermarket.api.delivery.signing.LicenceSigningService;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceRegistrationService {

    private final RegisteredDeviceRepository deviceRepository;
    private final UserProfileService userProfileService;
    private final LicenceSigningService signingService;

    public DeviceRegistrationService(
            RegisteredDeviceRepository deviceRepository,
            UserProfileService userProfileService,
            LicenceSigningService signingService
    ) {
        this.deviceRepository = deviceRepository;
        this.userProfileService = userProfileService;
        this.signingService = signingService;
    }

    @Transactional
    public DeviceResponse register(AuthenticatedUser user, RegisterDeviceRequest request) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        String hash = signingService.hashFingerprint(request.fingerprint());

        RegisteredDevice device = deviceRepository.findByBuyerUserIdAndFingerprintHash(profile.getId(), hash)
                .map(existing -> {
                    existing.markSeen();
                    return existing;
                })
                .orElseGet(() -> deviceRepository.save(
                        new RegisteredDevice(profile.getId(), hash, request.label())
                ));

        return DeviceResponse.from(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> listMine(AuthenticatedUser user) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        return deviceRepository.findByBuyerUserIdAndStatusOrderByRegisteredAtDesc(profile.getId(), DeviceStatus.ACTIVE)
                .stream()
                .map(DeviceResponse::from)
                .toList();
    }

    @Transactional
    public void revoke(AuthenticatedUser user, UUID deviceId) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        RegisteredDevice device = deviceRepository.findByIdAndBuyerUserId(deviceId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        device.revoke();
        deviceRepository.save(device);
    }

    @Transactional(readOnly = true)
    public RegisteredDevice requireActiveDevice(UUID buyerUserId, UUID deviceId) {
        RegisteredDevice device = deviceRepository.findByIdAndBuyerUserId(deviceId, buyerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        if (device.getStatus() != DeviceStatus.ACTIVE) {
            throw new AccessDeniedException("Device has been revoked");
        }
        return device;
    }
}
