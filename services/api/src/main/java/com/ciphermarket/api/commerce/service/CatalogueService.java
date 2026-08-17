package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.commerce.dto.CatalogueProductDetailResponse;
import com.ciphermarket.api.commerce.dto.CatalogueProductResponse;
import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CatalogueService {

    private final ProductRepository productRepository;

    public CatalogueService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogueProductResponse> listPublished(UUID categoryId) {
        List<Product> products = categoryId == null
                ? productRepository.findByStatusOrderByUpdatedAtDesc(ProductStatus.PUBLISHED)
                : productRepository.findByStatusAndCategoryIdOrderByUpdatedAtDesc(ProductStatus.PUBLISHED, categoryId);
        return products.stream().map(CatalogueProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CatalogueProductDetailResponse getPublishedProduct(UUID productId) {
        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return CatalogueProductDetailResponse.from(product);
    }
}
