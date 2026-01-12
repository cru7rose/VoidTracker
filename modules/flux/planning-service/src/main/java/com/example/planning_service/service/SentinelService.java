package com.example.planning_service.service;

import com.example.planning_service.entity.RouteEntity;
import com.example.planning_service.entity.RouteStopEntity;
import com.example.planning_service.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SentinelService {

    private final RouteRepository routeRepository;
    private final TravelTimeService travelTimeService;
    private final AlertService alertService;

    /**
     * Recalculates ETAs for all stops in a route based on current location and
     * time.
     */
    @Transactional
    public void recalculateRouteETA(UUID routeId) {
        RouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));

        if (route.getCurrentLocation() == null) {
            log.debug("Route {} has no current location. Skipping ETA calculation.", routeId);
            return;
        }

        List<RouteStopEntity> stops = route.getStops(); // Assuming relation exists
        stops.sort(Comparator.comparing(RouteStopEntity::getSequence));

        LocalDateTime currentTime = LocalDateTime.now();
        var currentLocation = route.getCurrentLocation();

        for (RouteStopEntity stop : stops) {
            // Skip completed stops logic would go here (requires status on stop)

            Duration travelTime = travelTimeService.calculateTravelTime(currentLocation, stop.getLocation());
            LocalDateTime arrivalTime = currentTime.plus(travelTime);

            stop.setPredictedArrivalTime(arrivalTime);

            checkSlaCompliance(route, stop);

            // Advance for next iteration
            currentTime = arrivalTime.plusMinutes(15); // Assume 15 min service time
            currentLocation = stop.getLocation();
        }

        route.setLastUpdated(LocalDateTime.now());
        route.setPredictedEndTime(currentTime);
        routeRepository.save(route);
    }

    private void checkSlaCompliance(RouteEntity route, RouteStopEntity stop) {
        if (stop.getSlaWindowEnd() != null && stop.getPredictedArrivalTime().isAfter(stop.getSlaWindowEnd())) {
            alertService.sendSlaViolationAlert(route.getId(), stop.getOrderId(), stop.getPredictedArrivalTime(),
                    stop.getSlaWindowEnd());
        }
    }
}
