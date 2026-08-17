package com.ciphermarket.api.product.repository;

import com.ciphermarket.api.product.domain.ProductAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductAssetRepository extends JpaRepository<ProductAsset, UUID> {

    List<ProductAsset> findByProductVersionId(UUID productVersionId);

    Optional<ProductAsset> findByIdAndOrganisationId(UUID id, UUID organisationId);
}
