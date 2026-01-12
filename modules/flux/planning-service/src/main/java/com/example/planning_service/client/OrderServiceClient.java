// File: planning-service/src/main/java/com/example/planning_service/client/OrderServiceClient.java
package com.example.planning_service.client;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.danxils_commons.dto.OrderQueryRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

/**
 * ARCHITEKTURA: Deklaratywny klient Feign do komunikacji z `order-service`.
 * Hermetyzuje logikę wywołań HTTP do wewnętrznego API order-service,
 * umożliwiając planning-service pobieranie zleceń do zaplanowania.
 * Nazwa 'order-service' odpowiada `spring.application.name` docelowej usługi.
 * Importuje DTO bezpośrednio z modułu danxils-commons.
 */
@FeignClient(name = "order-service")
public interface OrderServiceClient {

        @PostMapping("/api/internal/orders/query")
        List<OrderResponseDto> queryOrders(@RequestBody OrderQueryRequestDto query);

        @org.springframework.web.bind.annotation.GetMapping("/api/orders/{id}")
        OrderResponseDto getOrder(@org.springframework.web.bind.annotation.PathVariable("id") java.util.UUID id);

        @PostMapping("/api/orders/{orderId}/actions/confirm-delivery")
        OrderResponseDto confirmDelivery(
                        @org.springframework.web.bind.annotation.PathVariable("orderId") java.util.UUID orderId,
                        @RequestBody com.example.danxils_commons.dto.request.ConfirmDeliveryRequestDto request);

        @PostMapping("/api/orders/{orderId}/assign-driver")
        OrderResponseDto assignDriver(
                        @org.springframework.web.bind.annotation.PathVariable("orderId") java.util.UUID orderId,
                        @RequestBody com.example.planning_service.dto.request.AssignDriverRequestDto request);

        @PostMapping("/api/internal/orders/batch")
        List<OrderResponseDto> getOrdersBatch(@RequestBody List<java.util.UUID> orderIds);

        @org.springframework.web.bind.annotation.GetMapping("/api/orders/driver-assignments")
        java.util.Map<String, String> getDriverAssignments(
                        @org.springframework.web.bind.annotation.RequestParam("orderIds") List<java.util.UUID> orderIds);

}