package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "planning_planned_routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlannedRouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate planningDate;

    // Optional link to template if this route came from a Standard Route
    private UUID templateId;

    private String name;

    private UUID vehicleId;
    private String driverId;

    // Control Tower: Locking
    // If true, Timefold will NOT optimize this route (it's fixed by human)
    @Column(nullable = false)
    @Builder.Default
    private boolean locked = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<PlannedStop> stops;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlannedStop {
        private UUID orderId;
        private String type; // PICKUP, DELIVERY, DEPOT
        private Double lat;
        private Double lon;
        private Long plannedArrivalTime; 
        private boolean manual; // If true, added ad-hoc
    }
}
