package com.example.dashboard_service.dto;

/**
 * ARCHITEKTURA: DTO dla żądania przypisania kierowcy, używane w API
 * dashboard-service. Stanowi kontrakt dla frontendu panelu dyspozytorskiego.
 */
public record AssignDriverRequestDto(
        String driverId
) {
}