package com.ciphermarket.api.securityops.dto;

import com.ciphermarket.api.common.enums.ApprovalActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record CreateApprovalRequest(
        @NotNull ApprovalActionType actionType,
        @NotNull UUID resourceId,
        @NotBlank @Size(max = 2000) String reason,
        Map<String, Object> payload
) {
}
