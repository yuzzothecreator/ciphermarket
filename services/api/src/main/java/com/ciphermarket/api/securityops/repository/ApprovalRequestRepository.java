package com.ciphermarket.api.securityops.repository;

import com.ciphermarket.api.common.enums.ApprovalStatus;
import com.ciphermarket.api.securityops.domain.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {

    List<ApprovalRequest> findByStatusOrderByRequestedAtDesc(ApprovalStatus status);

    List<ApprovalRequest> findTop100ByOrderByRequestedAtDesc();
}
