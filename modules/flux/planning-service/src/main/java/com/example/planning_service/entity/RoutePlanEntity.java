// File: planning-service/src/main/java/com/example/planning_service/entity/RoutePlanEntity.java
package com.example.planning_service.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Encja reprezentująca szablon planu trasy. Jest to kluczowy
 * obiekt biznesowy w tym module, przechowujący zdefiniowane przez użytkownika
 * reguły (filtry) do grupowania zleceń oraz informacje o sposobie
 * i harmonogramie jego wykonania.
 */
@Entity
@Table(name = "planning_route_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlanEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerType triggerType;

    private String cronExpression;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private PlanFilters filters;

    /**
     * ARCHITEKTURA: Obiekt-wartość (Value Object) reprezentujący kryteria filtrowania.
     * Przechowywany jako JSONB w bazie danych, co zapewnia dużą elastyczność
     * w dodawaniu nowych typów filtrów w przyszłości bez zmiany schematu tabeli.
     */
    public record PlanFilters(
            List<UUID> customerIds,
            List<String> postalCodePrefixes,
            List<String> priorities
    ) implements Serializable {}
}