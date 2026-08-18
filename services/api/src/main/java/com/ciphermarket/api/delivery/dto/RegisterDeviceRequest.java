package com.ciphermarket.api.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDeviceRequest(
        @NotBlank @Size(max = 128) String fingerprint,
        @NotBlank @Size(max = 128) String label
) {
}
