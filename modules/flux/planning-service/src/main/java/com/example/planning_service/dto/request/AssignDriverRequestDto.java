package com.example.planning_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverRequestDto {
    @NotBlank(message = "Driver ID is required")
    private String driverId;

    // Optional: Driver Name, Vehicle ID, etc if needed by Order Service
}
