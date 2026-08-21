package com.ciphermarket.api.disclosure.dto;

import com.ciphermarket.api.common.enums.DisclosureRequestStatus;
import com.ciphermarket.api.disclosure.domain.DisclosureRequest;

import java.time.Instant;
import java.util.UUID;

public record DisclosureRequestResponse(
        UUID id,
        UUID organisationId,
        UUID documentId,
        String documentTitle,
        String documentSha256,
        Integer documentVersion,
        UUID createdByUserId,
        UUID recipientUserId,
        String recipientEmail,
        String confidentialityTerms,
        DisclosureRequestStatus status,
        Instant expiresAt,
        Instant acceptedAt,
        Instant rejectedAt,
        Instant revokedAt,
        Instant disclosedAt,
        Instant createdAt
) {
    public static DisclosureRequestResponse from(
            DisclosureRequest request,
            String documentTitle,
            String documentSha256,
            Integer documentVersion
    ) {
        return new DisclosureRequestResponse(
                request.getId(),
                request.getOrganisationId(),
                request.getDocumentId(),
                documentTitle,
                documentSha256,
                documentVersion,
                request.getCreatedByUserId(),
                request.getRecipientUserId(),
                request.getRecipientEmail(),
                request.getConfidentialityTerms(),
                request.getStatus(),
                request.getExpiresAt(),
                request.getAcceptedAt(),
                request.getRejectedAt(),
                request.getRevokedAt(),
                request.getDisclosedAt(),
                request.getCreatedAt()
        );
    }
}
