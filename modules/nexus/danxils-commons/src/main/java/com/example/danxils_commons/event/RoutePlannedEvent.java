// File: danxils-commons/src/main/java/com/example/danxils_commons/event/RoutePlannedEvent.java
package com.example.danxils_commons.event;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kanoniczne zdarzenie domenowe informujące o zaplanowaniu trasy.
 * Zostało wzbogacone o pole `vehicleId`, które jest kluczową informacją
 * dla konsumentów (np. order-service) do przypisania zasobu do zleceń.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlannedEvent {
    private UUID planId;
    private Instant createdAt;
    private UUID vehicleId; // POPRAWKA: Dodano kluczowe pole
    private List<UUID> includedOrderIds;
    private List<Waypoint> waypoints;
    private double totalDistanceMeters;
    private long totalTimeMillis;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Waypoint {
        private UUID orderId;
        private WaypointType type;
        private double latitude;
        private double longitude;
        private int sequence;
        private long estimatedArrivalTimeMillis;
    }

    public enum WaypointType {
        PICKUP,
        DELIVERY
    }
}