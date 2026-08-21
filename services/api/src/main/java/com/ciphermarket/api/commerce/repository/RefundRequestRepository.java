package com.ciphermarket.api.commerce.repository;

import com.ciphermarket.api.commerce.domain.RefundRequest;
import com.ciphermarket.api.common.enums.RefundRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, UUID> {

    List<RefundRequest> findByBuyerUserIdOrderByRequestedAtDesc(UUID buyerUserId);

    List<RefundRequest> findTop100ByOrderByRequestedAtDesc();

    List<RefundRequest> findByStatusOrderByRequestedAtDesc(RefundRequestStatus status);

    boolean existsByOrderIdAndStatusIn(UUID orderId, Collection<RefundRequestStatus> statuses);

    Optional<RefundRequest> findByIdAndBuyerUserId(UUID id, UUID buyerUserId);
}
