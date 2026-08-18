package com.ciphermarket.api.delivery.repository;

import com.ciphermarket.api.delivery.domain.Licence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicenceRepository extends JpaRepository<Licence, UUID> {

    Optional<Licence> findByEntitlementId(UUID entitlementId);

    Optional<Licence> findByIdAndBuyerUserId(UUID id, UUID buyerUserId);
}
