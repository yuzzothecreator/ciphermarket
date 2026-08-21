package com.ciphermarket.api.product.repository;

import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.common.enums.ProductType;
import com.ciphermarket.api.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByOrganisationIdOrderByUpdatedAtDesc(UUID organisationId);

    List<Product> findByStatusOrderByUpdatedAtDesc(ProductStatus status);

    Optional<Product> findByIdAndStatus(UUID id, ProductStatus status);

    List<Product> findByStatusAndCategoryIdOrderByUpdatedAtDesc(ProductStatus status, UUID categoryId);

    List<Product> findByStatusAndOrganisationIdOrderByUpdatedAtDesc(ProductStatus status, UUID organisationId);

    Optional<Product> findByIdAndOrganisationId(UUID id, UUID organisationId);

    boolean existsByOrganisationIdAndSlug(UUID organisationId, String slug);

    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:categoryId IS NULL OR p.categoryId = :categoryId)
              AND (:organisationId IS NULL OR p.organisationId = :organisationId)
              AND (:productType IS NULL OR p.productType = :productType)
              AND (:minPriceCents IS NULL OR p.priceCents >= :minPriceCents)
              AND (:maxPriceCents IS NULL OR p.priceCents <= :maxPriceCents)
              AND (
                   :q IS NULL OR :q = ''
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(p.shortDescription, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            """)
    List<Product> searchPublished(
            @Param("status") ProductStatus status,
            @Param("q") String q,
            @Param("categoryId") UUID categoryId,
            @Param("organisationId") UUID organisationId,
            @Param("productType") ProductType productType,
            @Param("minPriceCents") Long minPriceCents,
            @Param("maxPriceCents") Long maxPriceCents
    );
}
