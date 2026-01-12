package com.example.driver_app_service.dto;

import java.util.List;

/**
 * ARCHITEKTURA: DTO dla żądania potwierdzenia odbioru w API `driver-app-service`.
 * Stanowi kontrakt dla frontendu aplikacji webowej kierowcy.
 */
public record ConfirmPickupRequestDto(
        List<String> scannedBarcodes
) {
}