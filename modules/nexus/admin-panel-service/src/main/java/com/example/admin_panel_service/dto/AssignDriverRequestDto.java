package com.example.admin_panel_service.dto;

/**
 * ARCHITEKTURA: DTO dla żądania przypisania kierowcy, używane w API
 * dashboard-service. Stanowi kontrakt dla frontendu panelu dyspozytorskiego.
 */
public record AssignDriverRequestDto(
        String driverId
) {
}