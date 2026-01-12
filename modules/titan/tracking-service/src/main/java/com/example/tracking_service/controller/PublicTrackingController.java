package com.example.tracking_service.controller;

import com.example.danxils_commons.event.DriverLocationUpdatedEvent;
import com.example.tracking_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/tracking")
@RequiredArgsConstructor
public class PublicTrackingController {

    private final TrackingService trackingService;

    @GetMapping("/{token}")
    public ResponseEntity<DriverLocationUpdatedEvent> getTrackingInfo(@PathVariable String token) {
        // In a real app, 'token' would be a secure hash mapping to an orderId.
        // For this MVP, we assume token == orderId.
        DriverLocationUpdatedEvent event = trackingService.getLatestLocation(token);

        if (event == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(event);
    }
}
