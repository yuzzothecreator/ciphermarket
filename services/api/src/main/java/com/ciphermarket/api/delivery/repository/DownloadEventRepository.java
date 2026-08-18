package com.ciphermarket.api.delivery.repository;

import com.ciphermarket.api.delivery.domain.DownloadEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DownloadEventRepository extends JpaRepository<DownloadEvent, UUID> {

    List<DownloadEvent> findByBuyerUserIdOrderByCreatedAtDesc(UUID buyerUserId);
}
