package com.example.order_service.controller;

import com.example.danxils_commons.dto.OrderQueryRequestDto;
import com.example.order_service.model.dto.OrderResponseDto;
import com.example.order_service.entity.ClientEntity;
import com.example.order_service.entity.OrderEntity;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.repository.ClientRepository;
import com.example.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/orders")
@RequiredArgsConstructor
@Tag(name = "Internal Orders API", description = "APIs for service-to-service communication.")
public class InternalController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final ClientRepository clientRepository;

    @PostMapping("/query")
    @Operation(summary = "Query for orders based on a set of filters")
    public ResponseEntity<List<OrderResponseDto>> findOrdersByCriteria(@RequestBody OrderQueryRequestDto query) {
        List<OrderEntity> orders = orderService.findOrdersByCriteria(query);
        List<OrderResponseDto> dtos = orders.stream()
                .map(orderMapper::mapToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/batch")
    @Operation(summary = "Get multiple orders by IDs")
    public ResponseEntity<List<OrderResponseDto>> getOrdersBatch(@RequestBody List<UUID> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        // findOrdersByIds is valid as per OrderService update
        List<OrderEntity> orders = orderService.findOrdersByIds(orderIds);
        List<OrderResponseDto> dtos = orders.stream()
                .map(orderMapper::mapToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/clients") // Renamed from customers
    public ResponseEntity<ClientEntity> createClient(@RequestBody ClientEntity client) {
        return ResponseEntity.ok(clientRepository.save(client));
    }

    @PostMapping("/create")
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestBody com.example.order_service.dto.request.CreateOrderRequestDto request) {
        OrderEntity createdOrder = orderService.createOrder(request);
        return ResponseEntity.ok(orderMapper.mapToResponseDto(createdOrder));
    }
}