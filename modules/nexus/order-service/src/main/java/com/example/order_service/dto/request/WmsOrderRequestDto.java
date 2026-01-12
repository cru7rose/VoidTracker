package com.example.order_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * ARCHITEKTURA: DTO dla integracji z systemem WMS (Koła BMW).
 * Odzwierciedla strukturę JSON przesyłaną przez WMS.
 * Używa adnotacji Jacksona do mapowania polskich nazw pól na angielskie.
 */
public record WmsOrderRequestDto(
        @NotBlank(message = "Alias is required") @JsonProperty("alias") String alias,

        @NotBlank(message = "Serial number is required") @JsonProperty("nr seryjny") String serialNumber,

        @NotBlank(message = "Name is required") @JsonProperty("nazwa") String name, // Mapowane na wagę w logice
                                                                                    // biznesowej

        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") @JsonProperty("ilość") Integer quantity,

        @NotNull(message = "Warehouse acceptance date is required") @JsonProperty("data przyjęcia na magazyn") Instant warehouseAcceptanceDate) {
}
