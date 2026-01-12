package com.example.order_service.model.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class OrderAssignedEvent {
    private String orderId;
    private String driverId;
    private String assignedBy; // ID operatora
    private Instant timestamp;
}