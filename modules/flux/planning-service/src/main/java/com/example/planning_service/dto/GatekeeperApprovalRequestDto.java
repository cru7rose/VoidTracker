package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Gatekeeper approval flow
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatekeeperApprovalRequestDto {
    private String approvalId;
    private boolean approved;
    private String comment; // Optional user comment
}
