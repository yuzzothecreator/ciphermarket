package com.ciphermarket.api.securityops.repository;

import com.ciphermarket.api.common.enums.SecurityEventStatus;
import com.ciphermarket.api.securityops.domain.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {

    List<SecurityEvent> findTop100ByOrderByCreatedAtDesc();

    List<SecurityEvent> findTop100ByStatusOrderByCreatedAtDesc(SecurityEventStatus status);
}
