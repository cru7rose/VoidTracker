package com.example.planning_service.domain.timefold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Geographic location (lat/lon coordinates)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private Double latitude;
    private Double longitude;
    private String city;
    private String address;
}
