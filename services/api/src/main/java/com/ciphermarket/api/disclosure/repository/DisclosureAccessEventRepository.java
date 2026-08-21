package com.ciphermarket.api.disclosure.repository;

import com.ciphermarket.api.disclosure.domain.DisclosureAccessEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DisclosureAccessEventRepository extends JpaRepository<DisclosureAccessEvent, UUID> {

    List<DisclosureAccessEvent> findByRequestIdOrderByCreatedAtAsc(UUID requestId);

    List<DisclosureAccessEvent> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);
}
