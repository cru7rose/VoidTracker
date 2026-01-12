package com.example.order_service.model.event;

import com.example.danxils_commons.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class OrderStatusChangedEvent {
    private String orderId;
    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private String changedBy; // ID użytkownika (operatora, kierowcy)
    private Instant timestamp;
}