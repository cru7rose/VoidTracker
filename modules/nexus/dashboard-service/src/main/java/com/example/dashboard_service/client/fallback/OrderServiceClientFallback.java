package com.example.dashboard_service.client.fallback;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.danxils_commons.enums.OrderStatus;
import com.example.dashboard_service.client.OrderServiceClient;
import com.example.dashboard_service.dto.AssignDriverRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public Page<OrderResponseDto> getOrders(List<OrderStatus> statuses, String driverId, UUID customerId, int page, int size, String sort) {
        log.error("CIRCUIT BREAKER: Fallback for getOrders triggered. Order-service is unavailable. Returning empty page.");
        return new PageImpl<>(Collections.emptyList());
    }

    @Override
    public OrderResponseDto assignDriver(UUID orderId, AssignDriverRequestDto request) {
        log.error("CIRCUIT BREAKER: Fallback for assignDriver triggered for order {}. Order-service is unavailable.", orderId);
        throw new IllegalStateException("Order service is currently unavailable. Please try again later.");
    }
}