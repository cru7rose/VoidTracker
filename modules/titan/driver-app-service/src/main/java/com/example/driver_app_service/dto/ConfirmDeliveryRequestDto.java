package com.example.driver_app_service.dto;

import java.util.List;

/**
 * ARCHITEKTURA: DTO dla żądania potwierdzenia dostawy w API `driver-app-service`.
 * Definiuje złożony kontrakt, który pozwala kierowcy na jednoczesne
 * zaraportowanie dostarczonych paczek i wyników wykonanych usług dodatkowych.
 * Jego struktura odzwierciedla DTO oczekiwane przez `order-service`.
 */
public record ConfirmDeliveryRequestDto(
        List<String> deliveredBarcodes,
        List<PerformedServiceDto> performedServices
) {
    /**
     * ARCHITEKTURA: Zagnieżdżony rekord reprezentujący wynik
     * pojedynczej wykonanej usługi dodatkowej.
     */
    public record PerformedServiceDto(
            String serviceCode,
            Object result
    ) {}
}