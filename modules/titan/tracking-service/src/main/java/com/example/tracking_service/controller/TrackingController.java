// File: tracking-service/src/main/java/com/example/tracking_service/controller/TrackingController.java
package com.example.tracking_service.controller;

import com.example.tracking_service.dto.LocationUpdateRequest;
import com.example.tracking_service.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.danxils_commons.event.DriverLocationUpdatedEvent;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.security.Principal;

/**
 * ARCHITEKTURA: Kontroler REST dedykowany do obsługi aktualizacji
 * geolokalizacyjnych.
 * Udostępnia jeden, bezpieczny endpoint dla aplikacji mobilnej kierowcy.
 * Zwraca status `202 Accepted`, co jest semantycznie poprawną odpowiedzią dla
 * operacji,
 * która została przyjęta i będzie przetwarzana asynchronicznie.
 */
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/location")
    public ResponseEntity<Void> updateLocation(@Valid @RequestBody LocationUpdateRequest request, Principal principal) {
        String driverId = principal.getName();
        trackingService.processLocationUpdate(request, driverId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/fleet")
    public ResponseEntity<List<DriverLocationUpdatedEvent>> getFleetLocations() {
        return ResponseEntity.ok(trackingService.getFleetLocations());
    }
}