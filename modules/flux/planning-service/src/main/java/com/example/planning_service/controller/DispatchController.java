package com.example.planning_service.controller;

import com.example.planning_service.optimization.VrpOptimizerService;
import com.example.planning_service.service.DispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @PostMapping("/routes/{routeId}/orders/append")
    public ResponseEntity<VrpOptimizerService.VehicleRoutingSolution> appendOrder(
            @PathVariable String routeId,
            @RequestParam String orderId) {
        return ResponseEntity.ok(dispatchService.assignOrderToRoute(routeId, orderId));
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetPlan() {
        dispatchService.resetPlan();
        return ResponseEntity.ok().build();
    }
}
