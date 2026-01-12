package com.example.audit_service.service;

import com.example.audit_service.entity.AuditEventEntity;
import com.example.audit_service.repository.AuditEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ARCHITEKTURA: Serwis odpowiedzialny za logikę biznesową modułu audytu.
 * Jego głównym zadaniem jest transformacja przychodzących zdarzeń z Kafki
 * w ustandaryzowaną encję audytową oraz jej zapis do bazy danych. Udostępnia
 * również metody do odczytywania historii zdarzeń.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void recordEvent(String eventType, String entityType, String entityId, String userId, String serviceName, Object eventPayload) {
        try {
            AuditEventEntity auditEvent = new AuditEventEntity();
            auditEvent.setEventId(UUID.randomUUID());
            auditEvent.setTimestamp(Instant.now());
            auditEvent.setEventType(eventType);
            auditEvent.setEntityType(entityType);
            auditEvent.setEntityId(entityId);
            auditEvent.setUserId(userId);
            auditEvent.setServiceName(serviceName);

            @SuppressWarnings("unchecked")
            Map<String, Object> details = objectMapper.convertValue(eventPayload, Map.class);
            auditEvent.setDetails(details);

            auditEventRepository.save(auditEvent);
            log.info("Successfully recorded audit event of type {} for entity {}", eventType, entityId);
        } catch (Exception e) {
            log.error("Failed to record audit event of type {} for entity {}. Error: {}", eventType, entityId, e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditEventEntity> getHistoryForEntity(String entityType, String entityId) {
        log.debug("Fetching audit history for entityType: {}, entityId: {}", entityType, entityId);
        return auditEventRepository.findByEntityTypeAndEntityIdOrderByTimestampAsc(entityType, entityId);
    }
}