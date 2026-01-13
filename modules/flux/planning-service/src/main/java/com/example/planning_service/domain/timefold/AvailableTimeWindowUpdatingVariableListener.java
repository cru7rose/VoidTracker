package com.example.planning_service.domain.timefold;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Shadow Variable Listener - Calculates available time window for FixedRoute vehicles
 * Updates automatically when stops are added/removed from the route
 * 
 * For FixedRoute (Elastic Shell), calculates the time window where ad-hoc stops
 * can be inserted without violating fixed stop constraints.
 */
public class AvailableTimeWindowUpdatingVariableListener implements VariableListener<VehicleRoutingSolution, Vehicle> {

    @Override
    public void beforeEntityAdded(ScoreDirector<VehicleRoutingSolution> scoreDirector, Vehicle vehicle) {
        // No action needed
    }

    @Override
    public void afterEntityAdded(ScoreDirector<VehicleRoutingSolution> scoreDirector, Vehicle vehicle) {
        updateAvailableTimeWindow(scoreDirector, vehicle);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<VehicleRoutingSolution> scoreDirector, Vehicle vehicle) {
        // No action needed
    }

    @Override
    public void afterVariableChanged(ScoreDirector<VehicleRoutingSolution> scoreDirector, Vehicle vehicle) {
        updateAvailableTimeWindow(scoreDirector, vehicle);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<VehicleRoutingSolution> scoreDirector, Vehicle vehicle) {
        // No action needed
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<VehicleRoutingSolution> scoreDirector, Vehicle vehicle) {
        // No action needed
    }

    /**
     * Calculate and update available time window for FixedRoute vehicles
     * 
     * For FixedRoute:
     * - availableTimeWindowStart = end of last fixed stop + buffer
     * - availableTimeWindowEnd = start of next fixed stop - buffer
     * 
     * For regular vehicles:
     * - availableTimeWindowStart = now
     * - availableTimeWindowEnd = end of work day (e.g., 18:00)
     */
    private void updateAvailableTimeWindow(ScoreDirector<VehicleRoutingSolution> scoreDirector, Vehicle vehicle) {
        if (!vehicle.isFixedRoute()) {
            // Regular vehicle: full day available
            LocalDateTime workStart = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0);
            LocalDateTime workEnd = LocalDateTime.now().withHour(18).withMinute(0).withSecond(0);
            
            scoreDirector.beforeVariableChanged(vehicle, "availableTimeWindowStart");
            scoreDirector.beforeVariableChanged(vehicle, "availableTimeWindowEnd");
            vehicle.setAvailableTimeWindowStart(workStart);
            vehicle.setAvailableTimeWindowEnd(workEnd);
            scoreDirector.afterVariableChanged(vehicle, "availableTimeWindowStart");
            scoreDirector.afterVariableChanged(vehicle, "availableTimeWindowEnd");
            return;
        }

        // FixedRoute: Calculate gaps between fixed stops
        RouteStop firstStop = vehicle.getNextStop();
        
        if (firstStop == null) {
            // No stops assigned yet - full day available
            LocalDateTime workStart = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0);
            LocalDateTime workEnd = LocalDateTime.now().withHour(18).withMinute(0).withSecond(0);
            
            scoreDirector.beforeVariableChanged(vehicle, "availableTimeWindowStart");
            scoreDirector.beforeVariableChanged(vehicle, "availableTimeWindowEnd");
            vehicle.setAvailableTimeWindowStart(workStart);
            vehicle.setAvailableTimeWindowEnd(workEnd);
            scoreDirector.afterVariableChanged(vehicle, "availableTimeWindowStart");
            scoreDirector.afterVariableChanged(vehicle, "availableTimeWindowEnd");
            return;
        }

        // Find the largest gap between stops for ad-hoc insertion
        // For MVP: Use first available gap (between first and second stop)
        RouteStop current = firstStop;
        RouteStop next = current.getNextStop();
        
        LocalDateTime windowStart = null;
        LocalDateTime windowEnd = null;
        
        if (next != null && current.getArrivalTime() != null && next.getArrivalTime() != null) {
            // Gap between two stops
            long serviceTimeMinutes = 15; // Average service time
            windowStart = current.getArrivalTime().plusMinutes(serviceTimeMinutes);
            windowEnd = next.getArrivalTime().minusMinutes(serviceTimeMinutes);
            
            // Ensure minimum gap of 30 minutes for ad-hoc stop
            if (Duration.between(windowStart, windowEnd).toMinutes() < 30) {
                windowStart = null;
                windowEnd = null;
            }
        } else {
            // No gap found - use end of route
            if (current.getArrivalTime() != null) {
                long serviceTimeMinutes = 15;
                windowStart = current.getArrivalTime().plusMinutes(serviceTimeMinutes);
                windowEnd = LocalDateTime.now().withHour(18).withMinute(0).withSecond(0);
            }
        }
        
        scoreDirector.beforeVariableChanged(vehicle, "availableTimeWindowStart");
        scoreDirector.beforeVariableChanged(vehicle, "availableTimeWindowEnd");
        vehicle.setAvailableTimeWindowStart(windowStart);
        vehicle.setAvailableTimeWindowEnd(windowEnd);
        scoreDirector.afterVariableChanged(vehicle, "availableTimeWindowStart");
        scoreDirector.afterVariableChanged(vehicle, "availableTimeWindowEnd");
    }
}
