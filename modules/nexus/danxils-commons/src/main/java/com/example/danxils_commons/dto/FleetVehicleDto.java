package com.example.danxils_commons.dto;

import java.util.UUID;

/**
 * ARCHITEKTURA: Kanoniczny DTO reprezentujący pojazd we flocie.
 * Jest częścią współdzielonego jądra (shared kernel), stanowiąc kontrakt
 * danych używany przez `planning-service` (jako właściciela danych)
 * oraz `admin-panel-service` (do zarządzania nimi).
 */
public record FleetVehicleDto(
        UUID id,
        String name,
        Double capacityWeight,
        Double capacityVolume
) {
}