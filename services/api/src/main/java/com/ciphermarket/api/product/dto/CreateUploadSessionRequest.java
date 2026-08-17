package com.ciphermarket.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUploadSessionRequest(
        @NotNull UUID productVersionId,
        @NotBlank @Size(max = 512) String fileName,
        @NotBlank @Size(max = 128) String contentType
) {
}
