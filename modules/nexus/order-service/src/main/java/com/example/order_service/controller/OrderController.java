package com.example.order_service.controller;

import com.example.order_service.model.dto.OrderResponseDto;
import com.example.danxils_commons.enums.OrderStatus;
import com.example.danxils_commons.dto.ePoDDto;
import com.example.order_service.dto.filter.OrderFilterDto;
import com.example.order_service.dto.request.AssignDriverRequestDto;
import com.example.danxils_commons.dto.request.ConfirmDeliveryRequestDto;
import com.example.order_service.dto.request.ConfirmPickupRequestDto;
import com.example.order_service.dto.request.CreateOrderRequestDto;
import com.example.order_service.entity.OrderEntity;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.service.OrderService;
import com.example.order_service.service.ePoDService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.order_service.service.DeliveryService;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Publiczny kontroler REST dla modułu Order Service.
 * Stanowi główną bramę dla operacji na zleceniach, takich jak tworzenie,
 * odpytywanie i dodawanie Elektronicznych Potwierdzeń Dostawy (ePoD).
 * Odpowiedzialnością kontrolera jest walidacja przychodzących żądań,
 * delegowanie logiki biznesowej do warstwy serwisowej oraz formatowanie
 * odpowiedzi przy użyciu zdefiniowanych kontraktów DTO.
 * Został zaktualizowany, aby używać kanonicznego OrderResponseDto.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Public API for creating and retrieving orders.")
public class OrderController {

    private final OrderService orderService;
    private final ePoDService epodService;
    private final OrderMapper orderMapper;
    private final DeliveryService deliveryService;
    private final com.example.order_service.repository.OrderRepository orderRepository;

    @PostMapping
    @Operation(summary = "Create a new transport order")
    @ApiResponse(responseCode = "201", description = "Order created successfully")
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequestDto request) {
        OrderEntity newOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.mapToResponseDto(newOrder));
    }

    @PostMapping("/{orderId}/epod")
    @Operation(summary = "Add an Electronic Proof of Delivery (ePoD) to an order")
    @ApiResponse(responseCode = "201", description = "ePoD added and order status updated to POD")
    @ApiResponse(responseCode = "400", description = "Invalid ePoD data")
    @ApiResponse(responseCode = "403", description = "User is not authorized to perform this action")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<ePoDDto> addEpodToOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody ePoDDto epodDto,
            Principal principal) {

        String currentUsername = principal.getName();
        ePoDDto createdEpod = epodService.addEpod(orderId, epodDto, currentUsername);
        return new ResponseEntity<>(createdEpod, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get a paginated list of orders with optional filters")
    public ResponseEntity<Page<OrderResponseDto>> getOrders(
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
            Pageable pageable) {

        OrderFilterDto filter = OrderFilterDto.builder()
                .statuses(statuses).customerId(customerId).driverId(driverId)
                .dateFrom(dateFrom).dateTo(dateTo).build();

        Page<OrderEntity> orderPage = orderService.findAll(filter, pageable);
        Page<OrderResponseDto> dtoPage = orderPage.map(orderMapper::mapToResponseDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details by ID")
    @ApiResponse(responseCode = "200", description = "Order details found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable UUID orderId) {
        OrderEntity order = orderService.findOrderById(orderId);
        return ResponseEntity.ok(orderMapper.mapToResponseDto(order));
    }

    @PostMapping("/{orderId}/assign-driver")
    @Operation(summary = "Manually assign a driver to an order")
    @ApiResponse(responseCode = "200", description = "Driver assigned successfully and order status updated")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "409", description = "Order is not in a state that allows driver assignment")
    public ResponseEntity<OrderResponseDto> assignDriver(
            @PathVariable UUID orderId,
            @Valid @RequestBody AssignDriverRequestDto request,
            Principal principal) {

        String currentUsername = principal.getName(); // Identyfikator dyspozytora z tokenu JWT
        OrderEntity updatedOrder = orderService.assignDriver(orderId, request, currentUsername);
        return ResponseEntity.ok(orderMapper.mapToResponseDto(updatedOrder));
    }

    @PostMapping("/{orderId}/actions/confirm-delivery")
    @Operation(summary = "Confirm delivery of packages and performed services (for drivers)")
    @ApiResponse(responseCode = "200", description = "Delivery confirmed successfully")
    @ApiResponse(responseCode = "403", description = "User is not authorized to update this order")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderResponseDto> confirmDelivery(
            @PathVariable UUID orderId,
            @Valid @RequestBody ConfirmDeliveryRequestDto request,
            Principal principal) {

        String currentUsername = principal.getName();
        OrderEntity updatedOrder = deliveryService.confirmDelivery(orderId, request, currentUsername);
        return ResponseEntity.ok(orderMapper.mapToResponseDto(updatedOrder));
    }

    @PostMapping("/{orderId}/actions/confirm-pickup")
    @Operation(summary = "Confirm pickup of packages by scanning barcodes (for drivers)")
    @ApiResponse(responseCode = "200", description = "Pickup confirmed successfully, order status updated to LOAD")
    @ApiResponse(responseCode = "400", description = "Barcode mismatch or empty scan list")
    @ApiResponse(responseCode = "403", description = "User is not authorized to update this order")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "409", description = "Order is not in a state that allows pickup confirmation")
    public ResponseEntity<OrderResponseDto> confirmPickup(
            @PathVariable UUID orderId,
            @Valid @RequestBody ConfirmPickupRequestDto request,
            Principal principal) {

        String currentUsername = principal.getName();
        OrderEntity updatedOrder = orderService.confirmPickup(orderId, request, currentUsername);
        return ResponseEntity.ok(orderMapper.mapToResponseDto(updatedOrder));
    }

    @PatchMapping("/{orderId}/metrics")
    @Operation(summary = "Update order metrics (e.g. waiting time) for dynamic billing")
    @ApiResponse(responseCode = "200", description = "Metrics updated successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderResponseDto> updateMetrics(
            @PathVariable UUID orderId,
            @Valid @RequestBody com.example.order_service.dto.request.UpdateOrderMetricsRequestDto request) {
        OrderEntity updatedOrder = orderService.updateMetrics(orderId, request);
        return ResponseEntity.ok(orderMapper.mapToResponseDto(updatedOrder));
    }

    @GetMapping("/driver-assignments")
    @Operation(summary = "Get bulk driver assignments for list of orderIds")
    @ApiResponse(responseCode = "200", description = "Driver assignments retrieved")
    public ResponseEntity<java.util.Map<String, String>> getDriverAssignments(
            @RequestParam List<UUID> orderIds) {
        List<OrderEntity> orders = orderRepository.findAllById(orderIds);
        java.util.Map<String, String> assignments = orders.stream()
                .filter(o -> o.getAssignedDriver() != null)
                .collect(java.util.stream.Collectors.toMap(
                        o -> o.getOrderId().toString(),
                        o -> o.getAssignedDriver().toString()));
        return ResponseEntity.ok(assignments);
    }
}