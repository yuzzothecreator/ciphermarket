package com.ciphermarket.api.upload.messaging;

import java.util.UUID;

public record UploadProcessingMessage(
        UUID uploadSessionId,
        UUID assetId,
        UUID organisationId,
        String correlationId
) {
}
