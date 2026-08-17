package com.ciphermarket.api.product.repository;

import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.common.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByOrganisationIdOrderByUpdatedAtDesc(UUID organisationId);

    List<Product> findByStatusOrderByUpdatedAtDesc(ProductStatus status);

    Optional<Product> findByIdAndStatus(UUID id, ProductStatus status);

    List<Product> findByStatusAndCategoryIdOrderByUpdatedAtDesc(ProductStatus status, UUID categoryId);

    Optional<Product> findByIdAndOrganisationId(UUID id, UUID organisationId);

    boolean existsByOrganisationIdAndSlug(UUID organisationId, String slug);
}
