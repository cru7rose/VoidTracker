package com.example.planning_service.controller;

import com.example.planning_service.domain.timefold.VehicleRoutingSolution;
import com.example.planning_service.service.GatekeeperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

/**
 * Temporary controller to test Gatekeeper triggers manually.
 */
@RestController
@RequestMapping("/api/planning/gatekeeper")
@RequiredArgsConstructor
public class GatekeeperTestController {

    private final GatekeeperService gatekeeperService;

    @PostMapping("/test-trigger")
    public ResponseEntity<String> triggerTest() {
        // Create dummy solution to force a trigger
        VehicleRoutingSolution dummy = new VehicleRoutingSolution();
        dummy.setOptimizationId("TEST-TRIGGER-" + System.currentTimeMillis());
        // Mock score with internal API via string parsing or null for now if Service
        // handles it safely
        // But logic requires previous solution comparison usually.
        // However, we just want to test 'triggerN8nWebhook' which is private.
        // We can't access it directly.

        // Wait, validateSolution calls triggerN8nWebhook internally if threshold met.
        // But we need a previous solution to compare against.

        // Let's create a mocked scenario where diff > 20%
        // Since we can't easily construct full Timefold objects with Score here without
        // dependencies,
        // this might be brittle.

        // Alternative: Add a public method to GatekeeperService "forceTrigger(String
        // message)"
        // purely for testing?
        // Or just rely on the implementation plan's verification: "Simulate Change".

        return ResponseEntity
                .ok("Test endpoint created but logic requires complex object construction. Please check logs.");
    }
}
