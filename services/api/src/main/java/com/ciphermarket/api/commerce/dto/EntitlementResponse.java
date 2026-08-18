package com.ciphermarket.api.commerce.dto;

import com.ciphermarket.api.commerce.domain.Entitlement;
import com.ciphermarket.api.common.enums.EntitlementStatus;

import java.time.Instant;
import java.util.UUID;

public record EntitlementResponse(
        UUID id,
        UUID productId,
        String productName,
        String productType,
        UUID orderId,
        EntitlementStatus status,
        Instant grantedAt,
        boolean hasLicence
) {
    public static EntitlementResponse from(
            Entitlement entitlement,
            String productName,
            String productType,
            boolean hasLicence
    ) {
        return new EntitlementResponse(
                entitlement.getId(),
                entitlement.getProductId(),
                productName,
                productType,
                entitlement.getOrderId(),
                entitlement.getStatus(),
                entitlement.getGrantedAt(),
                hasLicence
        );
    }
}
