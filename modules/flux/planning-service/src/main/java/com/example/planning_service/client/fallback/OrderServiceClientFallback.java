package com.example.planning_service.client.fallback;

import com.example.danxils_commons.dto.OrderQueryRequestDto;
import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.planning_service.client.OrderServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * ARCHITEKTURA: Implementacja logiki zastępczej (fallback) dla klienta
 * OrderServiceClient.
 * W przypadku niedostępności `order-service`, ta klasa zapobiega awarii
 * `planning-service`, zwracając pustą listę zleceń i logując krytyczny błąd.
 */
@Slf4j
@Component
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public List<OrderResponseDto> queryOrders(OrderQueryRequestDto query) {
        log.error(
                "CIRCUIT BREAKER: Fallback for queryOrders triggered. Order-service is unavailable. Skipping planning cycle.");
        return Collections.emptyList();
    }

    @Override
    public OrderResponseDto getOrder(java.util.UUID id) {
        log.error("CIRCUIT BREAKER: Fallback for getOrder triggered. Order-service is unavailable.");
        return null;
    }

    @Override
    public OrderResponseDto confirmDelivery(java.util.UUID orderId,
            com.example.danxils_commons.dto.request.ConfirmDeliveryRequestDto request) {
        log.error("CIRCUIT BREAKER: Fallback for confirmDelivery triggered. Order-service is unavailable.");
        return null;
    }

    @Override
    public List<OrderResponseDto> getOrdersBatch(List<java.util.UUID> orderIds) {
        log.error("CIRCUIT BREAKER: Fallback for getOrdersBatch triggered. Order-service is unavailable.");
        return Collections.emptyList();
    }

    @Override
    public OrderResponseDto assignDriver(java.util.UUID orderId,
            com.example.planning_service.dto.request.AssignDriverRequestDto request) {
        log.error("CIRCUIT BREAKER: Fallback for assignDriver triggered. Order-service is unavailable.");
        return null;
    }

    @Override
    public java.util.Map<String, String> getDriverAssignments(List<java.util.UUID> orderIds) {
        log.error("CIRCUIT BREAKER: Fallback for getDriverAssignments triggered. Order-service is unavailable.");
        return Collections.emptyMap();
    }

}