package com.ciphermarket.api.securityops.dto;

import com.ciphermarket.api.common.enums.ApprovalActionType;
import com.ciphermarket.api.common.enums.ApprovalStatus;
import com.ciphermarket.api.securityops.domain.ApprovalRequest;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ApprovalRequestResponse(
        UUID id,
        ApprovalActionType actionType,
        String resourceType,
        UUID resourceId,
        UUID organisationId,
        Map<String, Object> payload,
        String reason,
        ApprovalStatus status,
        UUID requestedBy,
        UUID decidedBy,
        String decisionReason,
        Instant requestedAt,
        Instant decidedAt
) {
    public static ApprovalRequestResponse from(ApprovalRequest request) {
        return new ApprovalRequestResponse(
                request.getId(),
                request.getActionType(),
                request.getResourceType(),
                request.getResourceId(),
                request.getOrganisationId(),
                request.getPayload(),
                request.getReason(),
                request.getStatus(),
                request.getRequestedBy(),
                request.getDecidedBy(),
                request.getDecisionReason(),
                request.getRequestedAt(),
                request.getDecidedAt()
        );
    }
}
