package com.example.planning_service.domain.timefold;

import java.util.Comparator;

public class RouteStopDifficultyComparator implements Comparator<RouteStop> {
    @Override
    public int compare(RouteStop a, RouteStop b) {
        // Higher difficulty (harder to fit) comes first.
        // Example: Tighter time windows or larger demand are harder.

        // 1. Compare by demand (Volume) - Descending
        int volumeComp = compareDemand(a.getDemandVolume(), b.getDemandVolume());
        if (volumeComp != 0)
            return volumeComp;

        // 2. Compare by demand (Weight) - Descending
        int weightComp = compareDemand(a.getDemandWeight(), b.getDemandWeight());
        if (weightComp != 0)
            return weightComp;

        // 3. Compare by ID (stability)
        return a.getId().compareTo(b.getId());
    }

    private int compareDemand(java.math.BigDecimal a, java.math.BigDecimal b) {
        if (a == null && b == null)
            return 0;
        if (a == null)
            return -1; // null is easy (0)
        if (b == null)
            return 1;
        return a.compareTo(b);
    }
}
