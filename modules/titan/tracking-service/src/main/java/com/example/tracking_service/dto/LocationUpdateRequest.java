package com.example.tracking_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

/**
 * ARCHITEKTURA: DTO (Data Transfer Object) dla przychodzących żądań
 * aktualizacji
 * lokalizacji od aplikacji mobilnej kierowcy. Zawiera walidację, aby zapewnić
 * integralność podstawowych danych wejściowych.
 */
@Data
public class LocationUpdateRequest {

    @NotNull(message = "Order ID is required.")
    private UUID orderId;

    @NotNull(message = "Latitude is required.")
    private Double latitude;

    @NotNull(message = "Longitude is required.")
    private Double longitude;

    private Integer batteryLevel;
    private Integer signalStrength;
    private Double speed;
}