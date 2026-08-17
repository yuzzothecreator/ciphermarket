package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.commerce.domain.Entitlement;
import com.ciphermarket.api.commerce.domain.OrderItem;
import com.ciphermarket.api.commerce.repository.EntitlementRepository;
import com.ciphermarket.api.common.enums.EntitlementStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EntitlementService {

    private final EntitlementRepository entitlementRepository;

    public EntitlementService(EntitlementRepository entitlementRepository) {
        this.entitlementRepository = entitlementRepository;
    }

    @Transactional
    public Entitlement grant(UUID buyerUserId, OrderItem item) {
        return entitlementRepository.save(new Entitlement(
                buyerUserId,
                item.getProductId(),
                item.getOrderId(),
                item.getId()
        ));
    }

    @Transactional(readOnly = true)
    public boolean hasActiveEntitlement(UUID buyerUserId, UUID productId) {
        return entitlementRepository.existsByBuyerUserIdAndProductIdAndStatus(
                buyerUserId, productId, EntitlementStatus.ACTIVE
        );
    }
}
