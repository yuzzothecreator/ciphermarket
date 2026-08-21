package com.ciphermarket.api.disclosure.repository;

import com.ciphermarket.api.common.enums.DisclosureRequestStatus;
import com.ciphermarket.api.disclosure.domain.DisclosureRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DisclosureRequestRepository extends JpaRepository<DisclosureRequest, UUID> {

    List<DisclosureRequest> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);

    List<DisclosureRequest> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);

    Optional<DisclosureRequest> findByIdAndOrganisationId(UUID id, UUID organisationId);

    Optional<DisclosureRequest> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);

    boolean existsByDocumentIdAndRecipientUserIdAndStatus(
            UUID documentId,
            UUID recipientUserId,
            DisclosureRequestStatus status
    );
}
