package com.ciphermarket.api.disclosure.dto;

import com.ciphermarket.api.common.enums.DisclosureDocumentStatus;
import com.ciphermarket.api.disclosure.domain.DisclosureDocument;

import java.time.Instant;
import java.util.UUID;

public record DisclosureDocumentResponse(
        UUID id,
        UUID organisationId,
        String title,
        String description,
        String originalFileName,
        String sha256Checksum,
        int documentVersion,
        DisclosureDocumentStatus status,
        Long fileSizeBytes,
        Instant createdAt
) {
    public static DisclosureDocumentResponse from(DisclosureDocument document) {
        return new DisclosureDocumentResponse(
                document.getId(),
                document.getOrganisationId(),
                document.getTitle(),
                document.getDescription(),
                document.getOriginalFileName(),
                document.getSha256Checksum(),
                document.getDocumentVersion(),
                document.getStatus(),
                document.getFileSizeBytes(),
                document.getCreatedAt()
        );
    }
}
