package com.ciphermarket.api.commerce.dto;

import com.ciphermarket.api.commerce.domain.Entitlement;
import com.ciphermarket.api.common.enums.EntitlementStatus;

import java.time.Instant;
import java.util.UUID;

public record EntitlementResponse(
        UUID id,
        UUID productId,
        UUID orderId,
        EntitlementStatus status,
        Instant grantedAt
) {
    public static EntitlementResponse from(Entitlement entitlement) {
        return new EntitlementResponse(
                entitlement.getId(),
                entitlement.getProductId(),
                entitlement.getOrderId(),
                entitlement.getStatus(),
                entitlement.getGrantedAt()
        );
    }
}
