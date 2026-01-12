package com.example.driver_app_service.controller;

import com.example.driver_app_service.dto.ScanRequestDto;
import com.example.driver_app_service.service.GeofencingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class ScanController {

    private final GeofencingService geofencingService;

    @PostMapping
    public ResponseEntity<String> handleScan(@RequestBody ScanRequestDto request) {
        // TODO: Extract driverId from SecurityContext/JWT
        
        try {
            geofencingService.validateGeofence(request);
            return ResponseEntity.ok("Scan Accepted");
        } catch (GeofencingService.GeofenceException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }
}
