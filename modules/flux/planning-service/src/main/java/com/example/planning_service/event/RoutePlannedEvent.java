package com.example.planning_service.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Zdarzenie domenowe informujące o pomyślnym zaplanowaniu i zoptymalizowaniu trasy.
 * Zawiera uporządkowaną listę punktów do odwiedzenia oraz zlecenia przypisane do planu.
 * Jest fundamentem do dalszej automatyzacji procesów, np. przypisywania tras do kierowców.
 */
@Data
@Builder
public class RoutePlannedEvent {
    private UUID planId;
    private Instant createdAt;
    private List<UUID> includedOrderIds;
    private List<Waypoint> waypoints;
    private double totalDistanceMeters;
    private long totalTimeMillis;

    @Data
    @Builder
    public static class Waypoint {
        private UUID orderId; // Zlecenie powiązane z punktem
        private WaypointType type; // Odbiór czy dostawa?
        private double latitude;
        private double longitude;
        private int sequence; // Kolejność na trasie
        private long estimatedArrivalTimeMillis; // Czas od startu trasy
    }

    public enum WaypointType {
        PICKUP,
        DELIVERY
    }
}