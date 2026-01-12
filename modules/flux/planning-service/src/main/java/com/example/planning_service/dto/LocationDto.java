package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Location information
 * Used across various solution DTOs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    /**
     * Latitude coordinate
     */
    private Double latitude;

    /**
     * Longitude coordinate
     */
    private Double longitude;

    /**
     * City name
     */
    private String city;

    /**
     * Full address (optional)
     */
    private String address;
}
