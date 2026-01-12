// File: dashboard-service/src/main/java/com/example/dashboard_service/dto/DashboardDtos.java
package com.example.dashboard_service.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Plik zawierający definicje wszystkich DTO specyficznych dla API
 * dashboardu.
 * Użycie rekordów Javy zapewnia zwięzłość i niezmienność (immutability)
 * tych obiektów transferowych.
 */
public final class DashboardDtos {

        public record DashboardSummaryDto(
                        int newOrdersCount,
                        int inTransitOrdersCount,
                        int deliveredTodayCount,
                        int issuesCount) {
        }

        public record ActiveDriverLocationDto(
                        String driverId,
                        String driverName,
                        double latitude,
                        double longitude,
                        Instant lastSeen,
                        UUID currentOrderId) {
        }
}