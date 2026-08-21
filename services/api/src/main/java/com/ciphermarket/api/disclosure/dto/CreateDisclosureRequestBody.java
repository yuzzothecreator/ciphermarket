package com.ciphermarket.api.disclosure.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateDisclosureRequestBody(
        @NotBlank @Email @Size(max = 320) String recipientEmail,
        @NotBlank @Size(max = 20_000) String confidentialityTerms,
        Instant expiresAt
) {
}
