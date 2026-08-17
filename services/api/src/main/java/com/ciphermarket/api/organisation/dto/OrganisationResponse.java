package com.ciphermarket.api.organisation.dto;

import com.ciphermarket.api.common.enums.OrganisationStatus;
import com.ciphermarket.api.organisation.domain.Organisation;

import java.time.Instant;
import java.util.UUID;

public record OrganisationResponse(
        UUID id,
        String name,
        String slug,
        String description,
        OrganisationStatus status,
        UUID ownerUserId,
        Instant createdAt
) {
    public static OrganisationResponse from(Organisation organisation) {
        return new OrganisationResponse(
                organisation.getId(),
                organisation.getName(),
                organisation.getSlug(),
                organisation.getDescription(),
                organisation.getStatus(),
                organisation.getOwnerUserId(),
                organisation.getCreatedAt()
        );
    }
}
