// File: planning-service/src/main/java/com/example/planning_service/dto/RoutePlanDto.java
package com.example.planning_service.dto;

import com.example.planning_service.entity.RoutePlanEntity;
import com.example.planning_service.entity.TriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: DTO używane w API do tworzenia i aktualizowania szablonów planów tras.
 * Zawiera walidację, aby zapewnić integralność danych wejściowych
 * i hermetyzuje dane potrzebne do zdefiniowania RoutePlanEntity.
 */
public record RoutePlanDto(
        UUID id,

        @NotBlank(message = "Plan name cannot be blank")
        String name,

        @NotNull(message = "Trigger type cannot be null")
        TriggerType triggerType,

        String cronExpression,

        @NotNull(message = "Filters cannot be null")
        RoutePlanEntity.PlanFilters filters
) {}