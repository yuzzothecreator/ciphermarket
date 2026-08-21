package com.ciphermarket.api.securityops.service;

import com.ciphermarket.api.audit.service.AuditService;
import com.ciphermarket.api.common.enums.SecurityEventSeverity;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.security.AuthenticatedUser;
import com.ciphermarket.api.securityops.dto.CreateSuspiciousReportRequest;
import com.ciphermarket.api.securityops.dto.SuspiciousReportResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class SuspiciousActivityService {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "LEAK", "FRAUD", "ABUSE", "MALWARE", "OTHER"
    );

    private final SecurityEventService securityEventService;
    private final AuditService auditService;
    private final UserProfileService userProfileService;

    public SuspiciousActivityService(
            SecurityEventService securityEventService,
            AuditService auditService,
            UserProfileService userProfileService
    ) {
        this.securityEventService = securityEventService;
        this.auditService = auditService;
        this.userProfileService = userProfileService;
    }

    @Transactional
    public SuspiciousReportResponse submit(AuthenticatedUser user, CreateSuspiciousReportRequest request) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        String category = request.category().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Unsupported report category: " + request.category());
        }

        String resourceType = request.resourceType() == null || request.resourceType().isBlank()
                ? "General"
                : request.resourceType().trim();

        Map<String, Object> details = Map.of(
                "category", category,
                "details", request.details() == null ? "" : request.details().trim(),
                "reporterEmail", profile.getEmail()
        );

        securityEventService.record(
                null,
                profile.getId(),
                "BUYER_SUSPICIOUS_REPORT",
                SecurityEventSeverity.HIGH,
                resourceType,
                request.resourceId(),
                "[" + category + "] " + request.summary().trim(),
                details
        );

        auditService.record(
                null,
                profile.getId(),
                user.keycloakSub(),
                "SUSPICIOUS_ACTIVITY_REPORTED",
                resourceType,
                request.resourceId(),
                null,
                Map.of("category", category, "summary", request.summary().trim())
        );

        return new SuspiciousReportResponse(
                "BUYER_SUSPICIOUS_REPORT",
                category,
                request.summary().trim(),
                request.resourceId(),
                Instant.now()
        );
    }
}
