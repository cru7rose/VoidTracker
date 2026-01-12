package com.example.dashboard_service.client;

import com.example.danxils_commons.enums.OrderStatus;
import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.dashboard_service.client.fallback.OrderServiceClientFallback;
import com.example.dashboard_service.dto.AssignDriverRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Klient Feign do `order-service`, oczyszczony z ręcznego
 * przekazywania nagłówka autoryzacyjnego. Propagacja tokenu jest teraz zarządzana globalnie.
 */
@FeignClient(name = "order-service", fallback = OrderServiceClientFallback.class)
public interface OrderServiceClient {

    @GetMapping("/api/orders")
    Page<OrderResponseDto> getOrders(
            @RequestParam(value = "statuses", required = false) List<OrderStatus> statuses,
            @RequestParam(value = "driverId", required = false) String driverId,
            @RequestParam(value = "customerId", required = false) UUID customerId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
    );

    @PostMapping("/api/orders/{orderId}/assign-driver")
    OrderResponseDto assignDriver(
            @PathVariable("orderId") UUID orderId,
            @RequestBody AssignDriverRequestDto request
    );
}