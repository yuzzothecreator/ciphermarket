package com.ciphermarket.api.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank @Size(max = 255) String displayName,
        @Size(max = 10) String locale,
        @Size(max = 64) String timezone
) {
}
