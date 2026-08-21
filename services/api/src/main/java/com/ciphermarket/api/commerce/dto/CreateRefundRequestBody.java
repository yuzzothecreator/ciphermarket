package com.ciphermarket.api.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRefundRequestBody(
        @NotBlank @Size(max = 4000) String reason
) {
}
