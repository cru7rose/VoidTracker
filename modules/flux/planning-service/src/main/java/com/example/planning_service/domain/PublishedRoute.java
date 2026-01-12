package com.example.planning_service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "published_routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishedRoute {

    @Id
    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "route_id", nullable = false)
    private String routeId;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Stores the full route structure (stops, orders, locations) as a JSON blob.
     * This snapshot is what the PWA will download and store offline.
     */
    @Column(name = "route_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String routeJson;

    @Column(name = "is_claimed")
    @Builder.Default
    private boolean isClaimed = false;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;
}
