package com.ciphermarket.api.securityops.dto;

import java.time.Instant;
import java.util.UUID;

public record SuspiciousReportResponse(
        String eventType,
        String category,
        String summary,
        UUID resourceId,
        Instant submittedAt
) {
}
