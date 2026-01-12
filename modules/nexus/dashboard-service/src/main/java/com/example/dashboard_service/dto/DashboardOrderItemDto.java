package com.example.dashboard_service.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Dashboard order list item DTO
 */
public record DashboardOrderItemDto(
                UUID orderId,
                String status,
                String priority,
                String pickupCity,
                String deliveryCity,
                String assignedDriver,
                Instant sla) {
}
