package com.example.planning_service.controller;

import com.example.planning_service.service.SentinelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/planning/sentinel")
@RequiredArgsConstructor
public class SentinelController {

    private final SentinelService sentinelService;

    @PostMapping("/recalculate")
    public ResponseEntity<Void> recalculateRouteETA(@RequestBody Map<String, UUID> payload) {
        UUID routeId = payload.get("routeId");
        if (routeId == null) {
            return ResponseEntity.badRequest().build();
        }
        sentinelService.recalculateRouteETA(routeId);
        return ResponseEntity.ok().build();
    }
}
