package com.ciphermarket.api.organisation.dto;

import com.ciphermarket.api.common.enums.MembershipStatus;
import com.ciphermarket.api.common.enums.OrganisationRole;
import com.ciphermarket.api.organisation.domain.OrganisationMembership;

import java.time.Instant;
import java.util.UUID;

public record MembershipResponse(
        UUID id,
        UUID organisationId,
        UUID userId,
        OrganisationRole role,
        MembershipStatus status,
        Instant joinedAt
) {
    public static MembershipResponse from(OrganisationMembership membership) {
        return new MembershipResponse(
                membership.getId(),
                membership.getOrganisationId(),
                membership.getUserId(),
                membership.getRole(),
                membership.getStatus(),
                membership.getJoinedAt()
        );
    }
}
