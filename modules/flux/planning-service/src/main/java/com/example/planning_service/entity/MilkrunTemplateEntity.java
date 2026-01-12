package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "planning_milkrun_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilkrunTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String driverId; // Optional pre-assigned driver
    private UUID vehicleId; // Optional pre-assigned vehicle

    @Column(nullable = false)
    private String schedule; // e.g., "0 8 * * 1-5" (Cron) or "MONDAY,WEDNESDAY"

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<MilkrunStop> stops;

    private boolean active;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilkrunStop {
        private String customerId; // Link to customer if applicable
        private String address;
        private Double lat;
        private Double lon;
        private String activityType; // PICKUP, DELIVERY
    }
}
