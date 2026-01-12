package com.example.planning_service.domain.timefold;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

import java.time.LocalDateTime;

/**
 * Shadow Variable Listener - Calculates arrival times based on route chain
 * Updates automatically when previousStandstill changes
 */
public class ArrivalTimeUpdatingVariableListener implements VariableListener<VehicleRoutingSolution, RouteStop> {

    @Override
    public void beforeEntityAdded(ScoreDirector<VehicleRoutingSolution> scoreDirector, RouteStop routeStop) {
        // No action needed before entity is added
    }

    @Override
    public void afterEntityAdded(ScoreDirector<VehicleRoutingSolution> scoreDirector, RouteStop routeStop) {
        updateArrivalTime(scoreDirector, routeStop);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<VehicleRoutingSolution> scoreDirector, RouteStop routeStop) {
        // No action needed before variable changes
    }

    @Override
    public void afterVariableChanged(ScoreDirector<VehicleRoutingSolution> scoreDirector, RouteStop routeStop) {
        updateArrivalTime(scoreDirector, routeStop);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<VehicleRoutingSolution> scoreDirector, RouteStop routeStop) {
        // No action needed
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<VehicleRoutingSolution> scoreDirector, RouteStop routeStop) {
        // No action needed
    }

    /**
     * Calculate and update arrival time for this stop
     */
    private void updateArrivalTime(ScoreDirector<VehicleRoutingSolution> scoreDirector, RouteStop stop) {
        Standstill previous = stop.getPreviousStandstill();

        if (previous == null) {
            // Not assigned yet
            scoreDirector.beforeVariableChanged(stop, "arrivalTime");
            stop.setArrivalTime(null);
            scoreDirector.afterVariableChanged(stop, "arrivalTime");
            return;
        }

        LocalDateTime arrivalTime;

        if (previous instanceof Vehicle) {
            // First stop in route - start from current time
            arrivalTime = LocalDateTime.now().plusMinutes(stop.getTravelTimeMinutes());
        } else {
            // Subsequent stop - add travel time to previous stop's arrival + service time
            RouteStop previousStop = (RouteStop) previous;
            if (previousStop.getArrivalTime() == null) {
                arrivalTime = null;
            } else {
                long serviceTimeMinutes = 15; // Average service time at stop
                arrivalTime = previousStop.getArrivalTime()
                        .plusMinutes(serviceTimeMinutes)
                        .plusMinutes(stop.getTravelTimeMinutes());
            }
        }

        scoreDirector.beforeVariableChanged(stop, "arrivalTime");
        stop.setArrivalTime(arrivalTime);
        scoreDirector.afterVariableChanged(stop, "arrivalTime");

        // Cascade update to next stop
        RouteStop nextStop = stop.getNextStop();
        if (nextStop != null) {
            updateArrivalTime(scoreDirector, nextStop);
        }
    }
}
