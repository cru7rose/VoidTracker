package com.example.planning_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriverAuthResponse {
    private String token;
    private String driverId;
    private String email;
}
