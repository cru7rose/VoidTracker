package com.example.planning_service.domain.timefold;

/**
 * Base interface for elements in a route chain
 * Implemented by both Vehicle (start) and RouteStop (middle/end)
 */
public abstract class Standstill {

    /**
     * Get the location of this standstill point
     */
    public abstract Location getLocation();

    /**
     * Next stop in chain (shadow variable, auto-maintained by solver)
     */
    @ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable(sourceVariableName = "previousStandstill")
    protected RouteStop nextStop;

    public RouteStop getNextStop() {
        return nextStop;
    }

    public void setNextStop(RouteStop nextStop) {
        this.nextStop = nextStop;
    }
}
