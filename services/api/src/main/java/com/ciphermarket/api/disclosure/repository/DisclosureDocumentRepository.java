package com.ciphermarket.api.disclosure.repository;

import com.ciphermarket.api.disclosure.domain.DisclosureDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DisclosureDocumentRepository extends JpaRepository<DisclosureDocument, UUID> {

    List<DisclosureDocument> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);

    Optional<DisclosureDocument> findByIdAndOrganisationId(UUID id, UUID organisationId);
}
