package com.example.planning_service.optimization.strategy;

import com.example.planning_service.dto.OptimizationRequestDto;
import com.example.planning_service.dto.VehicleRouteDto;
import com.example.planning_service.entity.OptimizationProfileEntity;
import com.example.danxils_commons.dto.OrderResponseDto;
import java.util.List;

public interface OptimizationStrategy {
    String getModelName();

    List<VehicleRouteDto> optimize(OptimizationRequestDto request, List<OrderResponseDto> orders,
            OptimizationProfileEntity profile);
}
