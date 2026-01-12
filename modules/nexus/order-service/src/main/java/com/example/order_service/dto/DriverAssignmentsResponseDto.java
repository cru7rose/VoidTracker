package com.example.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for bulk driver assignments query.
 * Maps orderIds to driverIds.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignmentsResponseDto {
    /**
     * Key: orderId (String)
     * Value: driverId (String UUID)
     */
    private Map<String, String> assignments;
}
