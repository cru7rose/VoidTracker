package com.example.tracking_service.controller;

import com.example.tracking_service.dto.AlertRequest;
import com.example.tracking_service.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/tracking/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // Simple in-memory storage for MVP
    private final java.util.List<AlertRequest> activeAlerts = new java.util.concurrent.CopyOnWriteArrayList<>();

    @PostMapping
    public ResponseEntity<Void> reportAlert(@RequestBody AlertRequest request, Principal principal) {
        alertService.processAlert(request, principal.getName());
        activeAlerts.add(request); // Store for retrieval
        return ResponseEntity.accepted().build();
    }

    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<java.util.List<AlertRequest>> getActiveAlerts() {
        return ResponseEntity.ok(activeAlerts);
    }
}
