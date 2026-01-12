package com.example.planning_service.service;

import com.example.planning_service.dto.SlottingRequestDto;
import com.example.planning_service.dto.SlottingResponseDto;
import com.example.planning_service.entity.RouteEntity;
import com.example.planning_service.entity.RouteStopEntity;
import com.example.planning_service.repository.RouteRepository;
import com.example.planning_service.repository.RouteStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlottingService {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;

    // Hardcoded constraints for MVP - later move to Vehicle properties
    private static final double MAX_WEIGHT_CAPACITY = 2000.0; // kg

    @Transactional
    public SlottingResponseDto assignOrderToRoute(SlottingRequestDto request) {
        // 1. Fetch active routes (simplified: all routes for now)
        List<RouteEntity> activeRoutes = routeRepository.findAll();

        for (RouteEntity route : activeRoutes) {
            // 2. Calculate current load
            double currentLoad = calculateCurrentLoad(route);

            // 3. Check Capacity
            if (currentLoad + request.getWeight() <= MAX_WEIGHT_CAPACITY) {
                // 4. Assign
                createStop(route, request);
                return new SlottingResponseDto(true, route.getId(), "Assigned to route " + route.getId());
            }
        }

        return new SlottingResponseDto(false, null, "No suitable route found with capacity.");
    }

    private double calculateCurrentLoad(RouteEntity route) {
        // In a real scenario, this would sum up weight from all linked stops/orders.
        // For MVP, we'll assume we read a property or it's 0 if new.
        // Ideally, RouteEntity should cache this or we query the Order Service.
        // Here we just return a specialized property if it exists, else 0.
        return 0.0; // Placeholder: assumes empty trucks for MVP testing
    }

    private void createStop(RouteEntity route, SlottingRequestDto request) {
        RouteStopEntity stop = new RouteStopEntity();
        stop.setRoute(route);
        stop.setOrderId(request.getOrderId());
        stop.setSequence(route.getStops().size() + 1);
        stop.setPredictedArrivalTime(LocalDateTime.now().plusHours(1)); // Dummy ETA

        // Persist
        routeStopRepository.save(stop);

        // Update route timestamp
        route.setLastUpdated(LocalDateTime.now());
        routeRepository.save(route);
    }
}
