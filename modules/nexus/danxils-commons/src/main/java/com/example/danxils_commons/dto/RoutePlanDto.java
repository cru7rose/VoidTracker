package com.example.danxils_commons.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kanoniczny DTO reprezentujący szablon planu trasy.
 * Jest częścią współdzielonego jądra (shared kernel), stanowiąc kontrakt
 * danych używany zarówno przez `planning-service` (do wykonywania planów),
 * jak i `admin-panel-service` (do zarządzania nimi).
 */
public record RoutePlanDto(
        UUID id,
        String name,
        TriggerType triggerType,
        String cronExpression,
        PlanFilters filters
) {
    /**
     * ARCHITEKTURA: Enum definiujący możliwe sposoby uruchomienia planu trasy.
     * Jako część DTO, stanowi element współdzielonego kontraktu.
     */
    public enum TriggerType {
        MANUAL,
        SCHEDULED
    }

    /**
     * ARCHITEKTURA: Obiekt-wartość (Value Object) reprezentujący kryteria filtrowania.
     * Jest częścią nadrzędnego DTO i definiuje, jakie zlecenia mają być uwzględnione
     * podczas wykonywania planu.
     */
    public record PlanFilters(
            List<UUID> customerIds,
            List<String> postalCodePrefixes,
            List<String> priorities
    ) implements Serializable {}
}