package com.example.tracking_service.service;

import com.example.tracking_service.dto.AlertRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    public void processAlert(AlertRequest request, String driverId) {
        // In a real system, this would:
        // 1. Save to DB
        // 2. Push to WebSocket/Control Tower
        // 3. Send SMS/Email to Dispatcher

        log.error("CRITICAL ALERT RECEIVED from Driver {}: Type={}, Location=[{}, {}]",
                driverId, request.getType(), request.getLatitude(), request.getLongitude());

        // For MVP, we just log it. The dashboard would poll/listen for this.
    }
}
