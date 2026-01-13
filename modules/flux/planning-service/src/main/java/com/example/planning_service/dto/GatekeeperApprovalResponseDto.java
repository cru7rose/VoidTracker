package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Gatekeeper approval flow
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatekeeperApprovalResponseDto {
    private boolean approved;
    private String message;
}
