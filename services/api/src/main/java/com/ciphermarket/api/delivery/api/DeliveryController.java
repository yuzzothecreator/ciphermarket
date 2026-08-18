package com.ciphermarket.api.delivery.api;

import com.ciphermarket.api.delivery.dto.AccessGrantResponse;
import com.ciphermarket.api.delivery.dto.CreateAccessGrantRequest;
import com.ciphermarket.api.delivery.dto.DeviceResponse;
import com.ciphermarket.api.delivery.dto.DownloadEventResponse;
import com.ciphermarket.api.delivery.dto.LicenceResponse;
import com.ciphermarket.api.delivery.dto.RegisterDeviceRequest;
import com.ciphermarket.api.delivery.service.AccessGrantService;
import com.ciphermarket.api.delivery.service.DeliveryService;
import com.ciphermarket.api.delivery.service.DeviceRegistrationService;
import com.ciphermarket.api.delivery.service.LicenceService;
import com.ciphermarket.api.delivery.repository.DownloadEventRepository;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Delivery", description = "Secure product delivery")
public class DeliveryController {

    private final LicenceService licenceService;
    private final AccessGrantService accessGrantService;
    private final DeliveryService deliveryService;
    private final DeviceRegistrationService deviceRegistrationService;
    private final DownloadEventRepository downloadEventRepository;
    private final UserProfileService userProfileService;

    public DeliveryController(
            LicenceService licenceService,
            AccessGrantService accessGrantService,
            DeliveryService deliveryService,
            DeviceRegistrationService deviceRegistrationService,
            DownloadEventRepository downloadEventRepository,
            UserProfileService userProfileService
    ) {
        this.licenceService = licenceService;
        this.accessGrantService = accessGrantService;
        this.deliveryService = deliveryService;
        this.deviceRegistrationService = deviceRegistrationService;
        this.downloadEventRepository = downloadEventRepository;
        this.userProfileService = userProfileService;
    }

    @PostMapping("/api/v1/entitlements/{entitlementId}/licence")
    @Operation(summary = "Issue or retrieve delivery licence")
    public LicenceResponse issueLicence(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID entitlementId
    ) {
        return licenceService.issueIfMissing(user, entitlementId);
    }

    @PostMapping("/api/v1/entitlements/{entitlementId}/access-grants")
    @Operation(summary = "Create short-lived download access grant")
    public AccessGrantResponse createAccessGrant(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID entitlementId,
            @RequestBody(required = false) CreateAccessGrantRequest request
    ) {
        return accessGrantService.createGrant(user, entitlementId, request != null ? request : new CreateAccessGrantRequest(null));
    }

    @GetMapping("/api/v1/delivery/download")
    @Operation(summary = "Download product using access grant token")
    public ResponseEntity<Resource> download(
            @RequestParam("token") String token,
            HttpServletRequest request
    ) {
        return deliveryService.download(token, request);
    }

    @PostMapping("/api/v1/devices")
    @Operation(summary = "Register a buyer device")
    public DeviceResponse registerDevice(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RegisterDeviceRequest request
    ) {
        return deviceRegistrationService.register(user, request);
    }

    @GetMapping("/api/v1/devices")
    @Operation(summary = "List registered devices")
    public List<DeviceResponse> listDevices(@AuthenticationPrincipal AuthenticatedUser user) {
        return deviceRegistrationService.listMine(user);
    }

    @DeleteMapping("/api/v1/devices/{deviceId}")
    @Operation(summary = "Revoke a registered device")
    public void revokeDevice(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID deviceId
    ) {
        deviceRegistrationService.revoke(user, deviceId);
    }

    @GetMapping("/api/v1/downloads/history")
    @Operation(summary = "List download history for current buyer")
    public List<DownloadEventResponse> downloadHistory(@AuthenticationPrincipal AuthenticatedUser user) {
        var profile = userProfileService.requireProfileEntity(user.keycloakSub());
        return downloadEventRepository.findByBuyerUserIdOrderByCreatedAtDesc(profile.getId()).stream()
                .map(e -> new DownloadEventResponse(
                        e.getId(), e.getProductId(), null, e.getOutcome().name(), e.getCreatedAt()
                ))
                .toList();
    }
}
