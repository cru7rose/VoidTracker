package com.example.planning_service.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "planning_solutions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationSolutionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // DRAFT, PUBLISHED

    private String name; // e.g. "Plan #1234"

    // Summary Stats
    private Integer totalRoutes;
    private Integer totalStops;
    private Integer unassignedOrdersCount;
    private Double totalDistanceMeters;
    private Long totalDurationSeconds;

    public enum Status {
        DRAFT,
        PUBLISHED
    }

    @Type(JsonType.class)

    @Column(name = "solution_data", columnDefinition = "jsonb", nullable = true)
    private Object solutionJson;
}
