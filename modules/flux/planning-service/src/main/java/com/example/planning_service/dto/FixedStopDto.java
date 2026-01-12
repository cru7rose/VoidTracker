package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a fixed stop in a "Milkrun" or Fixed Route.
 * Used to calculate remaining capacity for the Elastic Shell.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixedStopDto {
    private Double lat;
    private Double lon;
    private BigDecimal weight;
    private BigDecimal volume;
    private String description;
}
