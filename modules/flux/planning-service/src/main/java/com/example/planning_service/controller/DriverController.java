package com.example.planning_service.controller;

import com.example.planning_service.entity.DriverSessionEntity;
import com.example.planning_service.entity.DriverWorkflowConfigEntity;
import com.example.planning_service.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/auth/generate-link")
    public ResponseEntity<String> generateLink(@RequestParam String driverId, @RequestParam String routeId,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(driverService.generateMagicLink(driverId, routeId, email));
    }

    @GetMapping("/auth/validate")
    public ResponseEntity<com.example.planning_service.dto.DriverAuthResponse> validateToken(
            @RequestParam String token) {
        return driverService.validateTokenAndGenerateJWT(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/config/{configCode}")
    public ResponseEntity<DriverWorkflowConfigEntity> getConfig(@PathVariable String configCode) {
        return driverService.getWorkflowConfig(configCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tasks")
    public ResponseEntity<java.util.List<com.example.planning_service.entity.DriverTaskEntity>> getMyTasks() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String userId = authentication.getName();
        // Assuming the 'sub' claim (email) is used as driverId for now
        return ResponseEntity.ok(driverService.getTasks(userId));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<com.example.planning_service.entity.DriverTaskEntity> completeTask(@PathVariable UUID taskId,
            @RequestBody com.example.planning_service.entity.DriverTaskEntity update) {
        return driverService.completeTask(taskId, update)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get driver's route overview for today
     */
    @GetMapping("/route")
    public ResponseEntity<com.example.planning_service.dto.RouteDetailsDTO> getDriverRoute() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String driverId = authentication.getName();
        return driverService.getDriverRoute(driverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Start the route (updates status to IN_PROGRESS)
     */
    @PostMapping("/route/start")
    public ResponseEntity<com.example.planning_service.dto.RouteDetailsDTO> startRoute() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String driverId = authentication.getName();
        return ResponseEntity.ok(driverService.startRoute(driverId));
    }

    /**
     * Stop/End the route (updates status to COMPLETED)
     */
    @PostMapping("/route/stop")
    public ResponseEntity<com.example.planning_service.dto.RouteDetailsDTO> stopRoute() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String driverId = authentication.getName();
        return ResponseEntity.ok(driverService.stopRoute(driverId));
    }
}
