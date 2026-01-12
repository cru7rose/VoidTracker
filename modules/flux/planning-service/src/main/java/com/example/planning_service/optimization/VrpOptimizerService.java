// File: planning-service/src/main/java/com/example/planning_service/optimization/VrpOptimizerService.java
package com.example.planning_service.optimization;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.planning_service.entity.FleetVehicleEntity;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Interfejs definiujący kontrakt dla serwisu optymalizacyjnego
 * VRP.
 * Oddziela logikę biznesową (w PlanningExecutionService) od konkretnej
 * implementacji
 * algorytmu VRP, co jest zgodne z zasadą odwrócenia zależności (Dependency
 * Inversion).
 * Zaktualizowano definicję VehicleRoutingSolution, aby stanowiła konkretny, typowany
 * kontrakt.
 */
public interface VrpOptimizerService {

        /**
         * ARCHITEKTURA: Obiekt-wartość (Value Object) reprezentujący kompletne,
         * zoptymalizowane
         * rozwiązanie problemu VRP. Jest to ustandaryzowany format wyjściowy,
         * niezależny od użytej biblioteki VRP.
         */
        record VehicleRoutingSolution(
                        List<Route> routes) {
                public record Route(
                                UUID vehicleId,
                                List<Activity> activities,
                                double totalDistance,
                                long totalTimeMillis,
                                String driverId) { // NEW: Driver assignment

                        // Constructor without driverId for backward compatibility
                        public Route(UUID vehicleId, List<Activity> activities, double totalDistance,
                                        long totalTimeMillis) {
                                this(vehicleId, activities, totalDistance, totalTimeMillis, null);
                        }
                }

                public record Activity(
                                UUID orderId,
                                ActivityType type,
                                double latitude,
                                double longitude,
                                long arrivalTimeMillis,
                                long endTimeMillis) {
                }

                public enum ActivityType {
                        PICKUP, DELIVERY
                }
        }

        /**
         * Główna metoda serwisu, która przyjmuje zlecenia i flotę,
         * a następnie zwraca zoptymalizowany plan tras.
         *
         * @param ordersToPlan Lista zleceń do uwzględnienia w planie.
         * @param fleet        Lista dostępnych pojazdów z ich pojemnościami.
         * @return Obiekt VehicleRoutingSolution zawierający zoptymalizowane trasy.
         */
        VehicleRoutingSolution calculateRoutes(List<OrderResponseDto> ordersToPlan, List<FleetVehicleEntity> fleet,
                        com.example.planning_service.entity.OptimizationProfileEntity profile);

        /**
         * Re-sequences a list of stops for a single vehicle starting from a specific
         * location.
         * Assumes items are already loaded (Delivery Only).
         */
        List<com.example.planning_service.dto.RouteStopDto> resequenceRoutes(
                        List<com.example.planning_service.dto.RouteStopDto> stops,
                        double startLat,
                        double startLon);
}