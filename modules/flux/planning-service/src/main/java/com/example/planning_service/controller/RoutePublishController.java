package com.example.planning_service.controller;

import com.example.planning_service.domain.PublishedRoute;
import com.example.planning_service.domain.timefold.VehicleRoutingSolution;
import com.example.planning_service.dto.PublishRouteRequestDto;
import com.example.planning_service.dto.PublishRouteResponseDto;
import com.example.planning_service.dto.VehicleRouteDto;
import com.example.planning_service.optimization.impl.TimefoldOptimizer;
import com.example.planning_service.repository.PublishedRouteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/planning/routes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow PWA access
public class RoutePublishController {

    private final TimefoldOptimizer optimizer;
    private final PublishedRouteRepository repository;
    private final ObjectMapper objectMapper;

    // TODO: Move this to config
    private static final String PWA_BASE_URL = "http://localhost:5174/claim";

    @PostMapping("/publish")
    public ResponseEntity<PublishRouteResponseDto> publishRoute(@RequestBody PublishRouteRequestDto request) {
        log.info("Publishing route for vehicle: {}", request.getVehicleId());

        // 1. Get cached solution
        VehicleRoutingSolution solution = optimizer.getLastSolution();
        if (solution == null) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Find the routes (Simplified DTOs)
        // Note: optimizer.mapToRoutes converts the logic solution to
        // List<VehicleRouteDto>
        List<VehicleRouteDto> routes = optimizer.mapToRoutes(solution);

        // 3. Find specific route
        VehicleRouteDto targetRoute = routes.stream()
                .filter(r -> r.getVehicleId() != null && r.getVehicleId().toString().equals(request.getVehicleId()))
                .findFirst()
                .orElse(null);

        if (targetRoute == null) {
            log.warn("Route for vehicle {} not found in solution", request.getVehicleId());
            // Fallback: try to match by string ID if UUID parsing fails or format differs
            targetRoute = routes.stream()
                    .filter(r -> checkMatch(r, request.getVehicleId()))
                    .findFirst()
                    .orElse(null);

            if (targetRoute == null) {
                return ResponseEntity.notFound().build();
            }
        }

        // 4. Generate Token & Save
        String token = UUID.randomUUID().toString();
        try {
            String routeJson = objectMapper.writeValueAsString(targetRoute);

            PublishedRoute publishedRoute = PublishedRoute.builder()
                    .token(token)
                    .routeId(targetRoute.getRouteId() != null ? targetRoute.getRouteId() : UUID.randomUUID().toString())
                    .driverName(request.getDriverName())
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .routeJson(routeJson)
                    .build();

            repository.save(publishedRoute);

            String magicLink = PWA_BASE_URL + "?token=" + token;

            return ResponseEntity.ok(PublishRouteResponseDto.builder()
                    .token(token)
                    .magicLink(magicLink)
                    .expiresAt(publishedRoute.getExpiresAt().toString())
                    .build());

        } catch (JsonProcessingException e) {
            log.error("Error serializing route", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/claim/{token}")
    public ResponseEntity<VehicleRouteDto> claimRoute(@PathVariable String token) {
        log.info("Claiming route with token: {}", token);

        return repository.findByToken(token)
                .map(published -> {
                    // Mark as claimed (optional logic)
                    if (!published.isClaimed()) {
                        published.setClaimed(true);
                        published.setClaimedAt(LocalDateTime.now());
                        repository.save(published);
                    }

                    try {
                        VehicleRouteDto route = objectMapper.readValue(published.getRouteJson(), VehicleRouteDto.class);
                        return ResponseEntity.ok(route);
                    } catch (JsonProcessingException e) {
                        log.error("Error deserializing route", e);
                        return ResponseEntity.internalServerError().<VehicleRouteDto>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private boolean checkMatch(VehicleRouteDto r, String reqId) {
        // Helper to check UUID vs String match
        if (r.getVehicleId() != null && r.getVehicleId().toString().equalsIgnoreCase(reqId))
            return true;
        if (r.getRouteId() != null && r.getRouteId().equalsIgnoreCase(reqId))
            return true;
        return false;
    }
}
