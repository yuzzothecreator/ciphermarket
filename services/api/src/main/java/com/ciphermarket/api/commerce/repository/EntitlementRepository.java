package com.ciphermarket.api.commerce.repository;

import com.ciphermarket.api.commerce.domain.Entitlement;
import com.ciphermarket.api.common.enums.EntitlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntitlementRepository extends JpaRepository<Entitlement, UUID> {

    List<Entitlement> findByBuyerUserIdOrderByGrantedAtDesc(UUID buyerUserId);

    List<Entitlement> findByBuyerUserIdAndStatusOrderByGrantedAtDesc(UUID buyerUserId, EntitlementStatus status);

    Optional<Entitlement> findByOrderItemId(UUID orderItemId);

    Optional<Entitlement> findByIdAndBuyerUserId(UUID id, UUID buyerUserId);

    List<Entitlement> findByOrderId(UUID orderId);

    boolean existsByBuyerUserIdAndProductIdAndStatus(UUID buyerUserId, UUID productId, EntitlementStatus status);
}
