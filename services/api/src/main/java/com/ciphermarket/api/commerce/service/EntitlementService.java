package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.commerce.domain.Entitlement;
import com.ciphermarket.api.commerce.domain.OrderItem;
import com.ciphermarket.api.commerce.repository.EntitlementRepository;
import com.ciphermarket.api.common.enums.EntitlementStatus;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EntitlementService {

    private final EntitlementRepository entitlementRepository;
    private final ProductRepository productRepository;

    public EntitlementService(
            EntitlementRepository entitlementRepository,
            ProductRepository productRepository
    ) {
        this.entitlementRepository = entitlementRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Entitlement grant(UUID buyerUserId, OrderItem item) {
        UUID versionId = productRepository.findById(item.getProductId())
                .map(Product::getCurrentVersionId)
                .orElse(null);
        return entitlementRepository.save(new Entitlement(
                buyerUserId,
                item.getProductId(),
                item.getOrderId(),
                item.getId(),
                versionId
        ));
    }

    @Transactional(readOnly = true)
    public boolean hasActiveEntitlement(UUID buyerUserId, UUID productId) {
        return entitlementRepository.existsByBuyerUserIdAndProductIdAndStatus(
                buyerUserId, productId, EntitlementStatus.ACTIVE
        );
    }
}
