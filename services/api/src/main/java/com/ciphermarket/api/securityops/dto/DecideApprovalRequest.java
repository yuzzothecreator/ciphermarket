package com.ciphermarket.api.securityops.dto;

import com.ciphermarket.api.common.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecideApprovalRequest(
        @NotNull ApprovalStatus decision,
        @Size(max = 2000) String decisionReason
) {
}
