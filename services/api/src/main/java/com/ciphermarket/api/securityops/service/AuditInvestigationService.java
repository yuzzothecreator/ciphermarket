package com.ciphermarket.api.securityops.service;

import com.ciphermarket.api.audit.domain.AuditEvent;
import com.ciphermarket.api.audit.repository.AuditEventRepository;
import com.ciphermarket.api.audit.service.AuditService;
import com.ciphermarket.api.common.enums.SecurityEventSeverity;
import com.ciphermarket.api.securityops.domain.AuditBatch;
import com.ciphermarket.api.securityops.dto.AuditBatchResponse;
import com.ciphermarket.api.securityops.dto.AuditEventResponse;
import com.ciphermarket.api.securityops.dto.AuditVerifyResponse;
import com.ciphermarket.api.securityops.repository.AuditBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.UUID;

@Service
public class AuditInvestigationService {

    private final AuditEventRepository auditEventRepository;
    private final AuditBatchRepository auditBatchRepository;
    private final AuditService auditService;
    private final SecurityEventService securityEventService;

    public AuditInvestigationService(
            AuditEventRepository auditEventRepository,
            AuditBatchRepository auditBatchRepository,
            AuditService auditService,
            SecurityEventService securityEventService
    ) {
        this.auditEventRepository = auditEventRepository;
        this.auditBatchRepository = auditBatchRepository;
        this.auditService = auditService;
        this.securityEventService = securityEventService;
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> listRecentEvents() {
        return auditEventRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(AuditEventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuditVerifyResponse verifyChain() {
        List<AuditEvent> events = auditEventRepository.findAllByOrderByCreatedAtAsc();
        if (events.isEmpty()) {
            return new AuditVerifyResponse(true, 0, "GENESIS", null, "No audit events");
        }

        String expectedPrevious = "GENESIS";
        for (AuditEvent event : events) {
            if (!expectedPrevious.equals(event.getPreviousHash())) {
                return new AuditVerifyResponse(
                        false,
                        events.size(),
                        event.getEventHash(),
                        event.getId().toString(),
                        "previous_hash mismatch"
                );
            }
            String recomputed = auditService.computeHash(
                    event.getId(),
                    event.getAction(),
                    event.getResourceType(),
                    event.getResourceId(),
                    event.getCorrelationId(),
                    event.getPreviousHash()
            );
            if (!recomputed.equals(event.getEventHash())) {
                return new AuditVerifyResponse(
                        false,
                        events.size(),
                        event.getEventHash(),
                        event.getId().toString(),
                        "event_hash mismatch"
                );
            }
            expectedPrevious = event.getEventHash();
        }

        return new AuditVerifyResponse(true, events.size(), expectedPrevious, null, "Chain intact");
    }

    @Transactional(readOnly = true)
    public List<AuditBatchResponse> listBatches() {
        return auditBatchRepository.findTop50ByOrderBySealedAtDesc().stream()
                .map(AuditBatchResponse::from)
                .toList();
    }

    @Transactional
    public AuditBatchResponse sealBatch(UUID sealedByUserId) {
        AuditVerifyResponse verify = verifyChain();
        if (!verify.intact()) {
            throw new IllegalStateException("Cannot seal a broken audit chain: " + verify.detail());
        }

        List<AuditEvent> all = auditEventRepository.findAllByOrderByCreatedAtAsc();
        if (all.isEmpty()) {
            throw new IllegalArgumentException("No audit events to seal");
        }

        AuditBatch previous = auditBatchRepository.findTopByOrderBySealedAtDesc().orElse(null);
        List<AuditEvent> unsealed = all;
        if (previous != null) {
            int lastIndex = indexOf(all, previous.getLastEventId());
            if (lastIndex < 0) {
                throw new IllegalStateException("Previous batch last event missing from chain");
            }
            unsealed = all.subList(lastIndex + 1, all.size());
        }

        if (unsealed.isEmpty()) {
            throw new IllegalArgumentException("No new audit events since last sealed batch");
        }

        String previousHash = previous == null ? "GENESIS" : previous.getRootHash();
        String rootHash = foldRootHash(unsealed, previousHash);
        AuditEvent first = unsealed.getFirst();
        AuditEvent last = unsealed.getLast();

        AuditBatch batch = auditBatchRepository.save(new AuditBatch(
                first.getId(),
                last.getId(),
                unsealed.size(),
                rootHash,
                previousHash,
                sealedByUserId
        ));

        securityEventService.record(
                null,
                sealedByUserId,
                "AUDIT_BATCH_SEALED",
                SecurityEventSeverity.MEDIUM,
                "AuditBatch",
                batch.getId(),
                "Audit batch sealed with " + unsealed.size() + " events",
                java.util.Map.of("rootHash", rootHash, "eventCount", unsealed.size())
        );

        auditService.record(
                null,
                sealedByUserId,
                null,
                "AUDIT_BATCH_SEALED",
                "AuditBatch",
                batch.getId(),
                null,
                java.util.Map.of("eventCount", unsealed.size(), "rootHash", rootHash)
        );

        return AuditBatchResponse.from(batch);
    }

    private int indexOf(List<AuditEvent> events, UUID id) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private String foldRootHash(List<AuditEvent> events, String previousBatchHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String acc = previousBatchHash;
            for (AuditEvent event : events) {
                acc = java.util.HexFormat.of().formatHex(
                        digest.digest((acc + "|" + event.getEventHash()).getBytes(StandardCharsets.UTF_8))
                );
                digest.reset();
            }
            return acc;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
