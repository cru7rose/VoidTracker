package com.example.planning_service.mapper;

import com.example.planning_service.domain.timefold.*;
import com.example.planning_service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manual mapper for VehicleRoutingSolution -> VehicleRoutingSolutionDto
 * 
 * MapStruct was not used here due to:
 * 1. Complex circular references requiring careful ID-based mapping
 * 2. Need for custom logic to handle Timefold shadow variables
 * 3. Performance optimization (single-pass mapping with caching)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SolutionMapper {

        /**
         * Convert domain VehicleRoutingSolution to DTO for JSON serialization
         * 
         * @param solution Solved VehicleRoutingSolution from Timefold
         * @return DTO with all metadata and chain references
         */
        public VehicleRoutingSolutionDto toDto(VehicleRoutingSolution solution) {
                if (solution == null) {
                        return null;
                }

                log.debug("Mapping VehicleRoutingSolution to DTO. Vehicles: {}, Stops: {}",
                                solution.getVehicles() != null ? solution.getVehicles().size() : 0,
                                solution.getStops() != null ? solution.getStops().size() : 0);

                // Build ID lookup maps for efficient reference resolution
                Map<String, Vehicle> vehicleMap = new HashMap<>();
                if (solution.getVehicles() != null) {
                        solution.getVehicles().forEach(v -> vehicleMap.put(v.getId(), v));
                }

                // Map vehicles
                List<VehicleSolutionDto> vehicleDtos = solution.getVehicles() != null
                                ? solution.getVehicles().stream()
                                                .map(this::mapVehicle)
                                                .collect(Collectors.toList())
                                : List.of();

                // Map stops
                List<RouteStopSolutionDto> stopDtos = solution.getStops() != null
                                ? solution.getStops().stream()
                                                .map(stop -> mapRouteStop(stop, vehicleMap))
                                                .collect(Collectors.toList())
                                : List.of();

                // Map depot
                LocationDto depotDto = solution.getDepot() != null
                                ? mapLocation(solution.getDepot())
                                : null;

                // Build solution DTO
                return VehicleRoutingSolutionDto.builder()
                                .problemId(solution.getOptimizationId())
                                .score(solution.getScore() != null ? solution.getScore().toString() : "N/A")
                                .unassignedStopsCount(solution.getUnassignedStopsCount())
                                .totalProfit(solution.getTotalProfit())
                                .vehicles(vehicleDtos)
                                .stops(stopDtos)
                                .depot(depotDto)
                                .orders(solution.getOrders())
                                // Solver metadata would come from a separate tracker if implemented
                                .solverMetadata(null)
                                .build();
        }

        /**
         * Map Vehicle entity to DTO
         */
        private VehicleSolutionDto mapVehicle(Vehicle vehicle) {
                if (vehicle == null) {
                        return null;
                }

                // Get nextStop ID (shadow variable)
                String nextStopId = vehicle.getNextStop() != null
                                ? vehicle.getNextStop().getId()
                                : null;

                return VehicleSolutionDto.builder()
                                .id(vehicle.getId())
                                .name(vehicle.getName())
                                .location(mapLocation(vehicle.getLocation()))
                                .capacityWeight(vehicle.getCapacityWeight())
                                .capacityVolume(vehicle.getCapacityVolume())
                                .skillSet(vehicle.getSkillSet())
                                .driverId(vehicle.getDriverId())
                                .driverName(vehicle.getDriverName())
                                .available(vehicle.isAvailable())
                                .nextStopId(nextStopId)
                                .build();
        }

        /**
         * Map RouteStop entity to DTO
         */
        private RouteStopSolutionDto mapRouteStop(RouteStop stop, Map<String, Vehicle> vehicleMap) {
                if (stop == null) {
                        return null;
                }

                // Map previousStandstill (can be Vehicle or RouteStop)
                RouteStopSolutionDto.StandstillReference prevRef = null;
                if (stop.getPreviousStandstill() != null) {
                        Standstill prev = stop.getPreviousStandstill();
                        if (prev instanceof Vehicle) {
                                Vehicle v = (Vehicle) prev;
                                prevRef = RouteStopSolutionDto.StandstillReference.builder()
                                                .type("Vehicle")
                                                .id(v.getId())
                                                .location(mapLocation(v.getLocation()))
                                                .build();
                        } else if (prev instanceof RouteStop) {
                                RouteStop rs = (RouteStop) prev;
                                prevRef = RouteStopSolutionDto.StandstillReference.builder()
                                                .type("RouteStop")
                                                .id(rs.getId())
                                                .location(mapLocation(rs.getLocation()))
                                                .build();
                        }
                }

                // Get vehicle ID (shadow variable - anchor)
                String vehicleId = stop.getVehicle() != null
                                ? stop.getVehicle().getId()
                                : null;

                // Get nextStop ID (shadow variable)
                String nextStopId = stop.getNextStop() != null
                                ? stop.getNextStop().getId()
                                : null;

                return RouteStopSolutionDto.builder()
                                .id(stop.getId())
                                .order(stop.getOrder())
                                .vehicleId(vehicleId)
                                .demandWeight(stop.getDemandWeight())
                                .demandVolume(stop.getDemandVolume())
                                .previousStandstill(prevRef)
                                .nextStopId(nextStopId)
                                .arrivalTime(stop.getArrivalTime())
                                .latitude(stop.getLatitude())
                                .longitude(stop.getLongitude())
                                .serviceDurationSeconds(stop.getServiceDurationSeconds())
                                .minStartTime(stop.getMinStartTime())
                                .maxEndTime(stop.getMaxEndTime())
                                .build();
        }

        /**
         * Map Location entity to DTO
         */
        private LocationDto mapLocation(Location location) {
                if (location == null) {
                        return null;
                }

                return LocationDto.builder()
                                .latitude(location.getLatitude())
                                .longitude(location.getLongitude())
                                .city(location.getCity())
                                .address(location.getAddress())
                                .build();
        }
}
