package com.example.danxils_commons.dto;

import java.util.UUID;

/**
 * ARCHITEKTURA: Kanoniczne DTO reprezentujące definicję usługi dodatkowej.
 * Jest częścią współdzielonego jądra (shared kernel), stanowiąc kontrakt danych
 * używany przez `order-service` (jako właściciela danych) oraz `admin-panel-service`
 * (do zarządzania słownikiem usług).
 */
public record AdditionalServiceDto(
        UUID id,
        String serviceCode,
        String name,
        String description,
        String inputType
) {
}