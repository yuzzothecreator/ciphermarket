package com.ciphermarket.api.product.repository;

import com.ciphermarket.api.product.domain.ProductVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVersionRepository extends JpaRepository<ProductVersion, UUID> {

    List<ProductVersion> findByProductIdOrderByCreatedAtDesc(UUID productId);

    Optional<ProductVersion> findByIdAndOrganisationId(UUID id, UUID organisationId);

    boolean existsByProductIdAndVersionLabel(UUID productId, String versionLabel);
}
