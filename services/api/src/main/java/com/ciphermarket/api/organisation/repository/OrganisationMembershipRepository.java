package com.ciphermarket.api.organisation.repository;

import com.ciphermarket.api.common.enums.MembershipStatus;
import com.ciphermarket.api.organisation.domain.OrganisationMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganisationMembershipRepository extends JpaRepository<OrganisationMembership, UUID> {

    Optional<OrganisationMembership> findByOrganisationIdAndUserId(UUID organisationId, UUID userId);

    List<OrganisationMembership> findByUserIdAndStatus(UUID userId, MembershipStatus status);

    List<OrganisationMembership> findByOrganisationIdAndStatus(UUID organisationId, MembershipStatus status);

    boolean existsByOrganisationIdAndUserIdAndStatus(UUID organisationId, UUID userId, MembershipStatus status);
}
