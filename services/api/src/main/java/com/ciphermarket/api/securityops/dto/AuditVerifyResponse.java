package com.ciphermarket.api.securityops.dto;

public record AuditVerifyResponse(
        boolean intact,
        int eventCount,
        String headHash,
        String brokenAtEventId,
        String detail
) {
}
