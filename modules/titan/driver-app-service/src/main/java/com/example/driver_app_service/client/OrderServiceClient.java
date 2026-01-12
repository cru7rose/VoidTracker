package com.example.driver_app_service.client;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.danxils_commons.enums.OrderStatus;
import com.example.driver_app_service.dto.ConfirmDeliveryRequestDto;
import com.example.driver_app_service.dto.ConfirmPickupRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Klient Feign do `order-service`, oczyszczony z ręcznego
 * przekazywania nagłówka autoryzacyjnego. Propagacja tokenu jest teraz
 * zarządzana globalnie.
 */
@FeignClient(name = "order-service")
public interface OrderServiceClient {

        @GetMapping("/api/orders")
        Page<OrderResponseDto> getOrders(
                        @RequestParam(value = "statuses") List<OrderStatus> statuses,
                        @RequestParam(value = "driverId") String driverId,
                        @RequestParam("page") int page,
                        @RequestParam("size") int size,
                        @RequestParam("sort") String sort);

        @GetMapping("/api/orders/{orderId}")
        OrderResponseDto getOrderById(@PathVariable("orderId") UUID orderId);

        @PostMapping("/api/orders/{orderId}/actions/confirm-delivery")
        OrderResponseDto confirmDelivery(
                        @PathVariable("orderId") UUID orderId,
                        @RequestBody ConfirmDeliveryRequestDto request);

        @PostMapping("/api/orders/{orderId}/actions/confirm-pickup")
        OrderResponseDto confirmPickup(
                        @PathVariable("orderId") UUID orderId,
                        @RequestBody ConfirmPickupRequestDto request);

        @PostMapping("/api/orders/{orderId}/epod")
        com.example.danxils_commons.dto.ePoDDto addEpod(
                        @PathVariable("orderId") UUID orderId,
                        @RequestBody com.example.danxils_commons.dto.ePoDDto epodDto);
}