package com.example.audit_service.controller;

import com.example.audit_service.entity.AuditEventEntity;
import com.example.audit_service.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ARCHITEKTURA: Kontroler REST udostępniający zabezpieczone, read-only API
 * do odpytywania historii zdarzeń audytowych. Dostęp do tych zasobów jest
 * ograniczony do ról administracyjnych w konfiguracji bezpieczeństwa.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/events")
    public ResponseEntity<List<AuditEventEntity>> getAuditEvents(
            @RequestParam String entityType,
            @RequestParam String entityId) {
        List<AuditEventEntity> history = auditService.getHistoryForEntity(entityType, entityId);
        return ResponseEntity.ok(history);
    }
}