package com.example.tracking_service.controller;

import com.example.tracking_service.dto.TerminalScanRequest;
import com.example.tracking_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking/scan")
@RequiredArgsConstructor
public class TerminalScanController {

    private final TrackingService trackingService;

    @PostMapping
    public ResponseEntity<Void> ingestScan(@RequestBody TerminalScanRequest request) {
        trackingService.processTerminalScan(request);
        return ResponseEntity.accepted().build();
    }
}
