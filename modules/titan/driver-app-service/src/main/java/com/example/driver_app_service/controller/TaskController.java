package com.example.driver_app_service.controller;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.driver_app_service.dto.ConfirmDeliveryRequestDto;
import com.example.driver_app_service.dto.ConfirmPickupRequestDto;
import com.example.driver_app_service.dto.DriverTaskDto;
import com.example.driver_app_service.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kontroler REST dla aplikacji kierowcy, oczyszczony z ręcznego
 * zarządzania tokenami. Informacje o zalogowanym użytkowniku (kierowcy)
 * są pobierane z obiektu Principal, a propagacja tokenu do Feign jest
 * automatyczna.
 */
@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/tasks")
    public ResponseEntity<List<DriverTaskDto>> getMyTasks(Principal principal) {
        String driverId = principal.getName();
        List<DriverTaskDto> tasks = taskService.getDriverTasks(driverId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/tasks/{orderId}")
    public ResponseEntity<OrderResponseDto> getTaskDetails(
            @PathVariable UUID orderId,
            Principal principal) {
        String driverId = principal.getName();
        OrderResponseDto taskDetails = taskService.getTaskDetails(orderId, driverId);
        return ResponseEntity.ok(taskDetails);
    }

    @PostMapping("/tasks/{orderId}/confirm-pickup")
    public ResponseEntity<OrderResponseDto> confirmPickup(
            @PathVariable UUID orderId,
            @RequestBody ConfirmPickupRequestDto request,
            Principal principal) {
        String driverId = principal.getName();
        OrderResponseDto updatedOrder = taskService.confirmPickup(orderId, driverId, request);
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/tasks/{orderId}/confirm-delivery")
    public ResponseEntity<OrderResponseDto> confirmDelivery(
            @PathVariable UUID orderId,
            @RequestBody ConfirmDeliveryRequestDto request,
            Principal principal) {
        String driverId = principal.getName();
        OrderResponseDto updatedOrder = taskService.confirmDelivery(orderId, driverId, request);
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/tasks/{orderId}/epod")
    public ResponseEntity<com.example.danxils_commons.dto.ePoDDto> addEpod(
            @PathVariable UUID orderId,
            @RequestBody com.example.danxils_commons.dto.ePoDDto epodDto,
            Principal principal) {
        String driverId = principal.getName();
        com.example.danxils_commons.dto.ePoDDto createdEpod = taskService.addEpod(orderId, driverId, epodDto);
        return ResponseEntity.ok(createdEpod);
    }
}