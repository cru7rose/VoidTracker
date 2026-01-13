package com.example.planning_service.controller;

import com.example.planning_service.dto.GatekeeperApprovalRequestDto;
import com.example.planning_service.dto.GatekeeperApprovalResponseDto;
import com.example.planning_service.service.GatekeeperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Gatekeeper Controller - User Approval Flow
 * 
 * Endpoints for Vue UI to approve/reject optimization solutions
 */
@RestController
@RequestMapping("/api/planning/gatekeeper")
@RequiredArgsConstructor
public class GatekeeperController {

    private final GatekeeperService gatekeeperService;

    /**
     * Process user approval/rejection of optimization solution
     * Called from Vue UI when user clicks Confirm/Reject
     */
    @PostMapping("/approve")
    public ResponseEntity<GatekeeperApprovalResponseDto> approveSolution(
            @RequestBody GatekeeperApprovalRequestDto request) {
        
        GatekeeperApprovalResponseDto response = gatekeeperService.processApproval(request);
        return ResponseEntity.ok(response);
    }
}
