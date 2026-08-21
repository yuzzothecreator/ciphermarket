package com.ciphermarket.api.commerce.dto;

import com.ciphermarket.api.commerce.domain.RefundRequest;
import com.ciphermarket.api.common.enums.RefundRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record RefundRequestResponse(
        UUID id,
        UUID orderId,
        UUID paymentId,
        UUID buyerUserId,
        UUID organisationId,
        long amountCents,
        String currency,
        String reason,
        RefundRequestStatus status,
        String rejectionReason,
        UUID approvalRequestId,
        String providerRefundRef,
        Instant requestedAt,
        Instant decidedAt,
        Instant completedAt
) {
    public static RefundRequestResponse from(RefundRequest request) {
        return new RefundRequestResponse(
                request.getId(),
                request.getOrderId(),
                request.getPaymentId(),
                request.getBuyerUserId(),
                request.getOrganisationId(),
                request.getAmountCents(),
                request.getCurrency(),
                request.getReason(),
                request.getStatus(),
                request.getRejectionReason(),
                request.getApprovalRequestId(),
                request.getProviderRefundRef(),
                request.getRequestedAt(),
                request.getDecidedAt(),
                request.getCompletedAt()
        );
    }
}
