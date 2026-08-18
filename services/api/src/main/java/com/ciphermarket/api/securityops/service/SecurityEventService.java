package com.ciphermarket.api.securityops.service;

import com.ciphermarket.api.common.enums.SecurityEventSeverity;
import com.ciphermarket.api.common.enums.SecurityEventStatus;
import com.ciphermarket.api.securityops.domain.SecurityEvent;
import com.ciphermarket.api.securityops.dto.SecurityEventResponse;
import com.ciphermarket.api.securityops.repository.SecurityEventRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;

    public SecurityEventService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    @Transactional
    public void record(
            UUID organisationId,
            UUID actorUserId,
            String eventType,
            SecurityEventSeverity severity,
            String resourceType,
            UUID resourceId,
            String summary,
            Map<String, Object> details
    ) {
        String correlationId = MDC.get("correlationId");
        securityEventRepository.save(new SecurityEvent(
                organisationId,
                actorUserId,
                eventType,
                severity,
                resourceType,
                resourceId,
                summary,
                details,
                correlationId
        ));
    }

    @Transactional(readOnly = true)
    public List<SecurityEventResponse> listRecent(SecurityEventStatus status) {
        List<SecurityEvent> events = status == null
                ? securityEventRepository.findTop100ByOrderByCreatedAtDesc()
                : securityEventRepository.findTop100ByStatusOrderByCreatedAtDesc(status);
        return events.stream().map(SecurityEventResponse::from).toList();
    }

    @Transactional
    public SecurityEventResponse acknowledge(UUID eventId, UUID actorUserId) {
        SecurityEvent event = securityEventRepository.findById(eventId)
                .orElseThrow(() -> new com.ciphermarket.api.common.exception.ResourceNotFoundException("Security event not found"));
        event.acknowledge(actorUserId);
        return SecurityEventResponse.from(securityEventRepository.save(event));
    }
}
