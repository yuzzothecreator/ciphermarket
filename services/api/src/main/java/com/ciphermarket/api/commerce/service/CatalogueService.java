package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.commerce.dto.CatalogueProductDetailResponse;
import com.ciphermarket.api.commerce.dto.CatalogueProductResponse;
import com.ciphermarket.api.commerce.dto.CatalogueSort;
import com.ciphermarket.api.commerce.dto.CreatorStorefrontResponse;
import com.ciphermarket.api.common.enums.OrganisationStatus;
import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.common.enums.ProductType;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.organisation.domain.Organisation;
import com.ciphermarket.api.organisation.repository.OrganisationRepository;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CatalogueService {

    private final ProductRepository productRepository;
    private final OrganisationRepository organisationRepository;

    public CatalogueService(
            ProductRepository productRepository,
            OrganisationRepository organisationRepository
    ) {
        this.productRepository = productRepository;
        this.organisationRepository = organisationRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogueProductResponse> search(
            String q,
            UUID categoryId,
            UUID organisationId,
            ProductType productType,
            Long minPriceCents,
            Long maxPriceCents,
            CatalogueSort sort
    ) {
        String query = q == null || q.isBlank() ? null : q.trim();
        List<Product> products = productRepository.searchPublished(
                ProductStatus.PUBLISHED,
                query,
                categoryId,
                organisationId,
                productType,
                minPriceCents,
                maxPriceCents
        );
        return sortProducts(products, sort == null ? CatalogueSort.NEWEST : sort).stream()
                .map(CatalogueProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogueProductDetailResponse getPublishedProduct(UUID productId) {
        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        String organisationSlug = organisationRepository.findById(product.getOrganisationId())
                .map(Organisation::getSlug)
                .orElse(null);
        return CatalogueProductDetailResponse.from(product, organisationSlug);
    }

    @Transactional(readOnly = true)
    public CreatorStorefrontResponse getStorefront(String slug) {
        Organisation organisation = organisationRepository.findBySlug(slug)
                .filter(org -> org.getStatus() == OrganisationStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));

        List<CatalogueProductResponse> products = productRepository
                .findByStatusAndOrganisationIdOrderByUpdatedAtDesc(ProductStatus.PUBLISHED, organisation.getId())
                .stream()
                .map(CatalogueProductResponse::from)
                .toList();

        return new CreatorStorefrontResponse(
                new CreatorStorefrontResponse.CreatorStorefrontOrganisation(
                        organisation.getId(),
                        organisation.getName(),
                        organisation.getSlug(),
                        organisation.getDescription()
                ),
                products
        );
    }

    private List<Product> sortProducts(List<Product> products, CatalogueSort sort) {
        Comparator<Product> comparator = switch (sort) {
            case PRICE_ASC -> Comparator.comparingLong(Product::getPriceCents);
            case PRICE_DESC -> Comparator.comparingLong(Product::getPriceCents).reversed();
            case NAME_ASC -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
            case NEWEST -> Comparator.comparing(Product::getUpdatedAt).reversed();
        };
        return products.stream().sorted(comparator).toList();
    }
}
