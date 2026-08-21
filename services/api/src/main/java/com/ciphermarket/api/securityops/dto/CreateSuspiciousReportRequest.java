package com.ciphermarket.api.securityops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSuspiciousReportRequest(
        @NotBlank @Size(max = 64) String category,
        @NotBlank @Size(max = 2000) String summary,
        @Size(max = 64) String resourceType,
        UUID resourceId,
        @Size(max = 4000) String details
) {
}
