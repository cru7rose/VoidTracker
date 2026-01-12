package com.example.driver_app_service.dto;

import com.example.danxils_commons.enums.OrderStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Dedykowane DTO dla listy zadań kierowcy.
 * Jest to uproszczona, "spłaszczona" reprezentacja zlecenia, zawierająca
 * tylko te informacje, które są niezbędne w widoku listy zadań,
 * co optymalizuje ilość przesyłanych danych.
 */
public record DriverTaskDto(
        UUID orderId,
        OrderStatus status,
        String pickupCity,
        String pickupStreet,
        String deliveryCity,
        String deliveryStreet,
        Instant sla
) {
}