package com.ciphermarket.api.organisation.repository;

import com.ciphermarket.api.common.enums.MembershipStatus;
import com.ciphermarket.api.organisation.domain.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Optional<Organisation> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Organisation> findByOwnerUserId(UUID ownerUserId);
}
