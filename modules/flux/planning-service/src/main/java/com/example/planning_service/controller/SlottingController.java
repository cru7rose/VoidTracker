package com.example.planning_service.controller;

import com.example.planning_service.dto.SlottingRequestDto;
import com.example.planning_service.dto.SlottingResponseDto;
import com.example.planning_service.service.SlottingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planning/slotting")
@RequiredArgsConstructor
public class SlottingController {

    private final SlottingService slottingService;

    @PostMapping("/assign")
    public ResponseEntity<SlottingResponseDto> assignOrder(@RequestBody SlottingRequestDto request) {
        SlottingResponseDto response = slottingService.assignOrderToRoute(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
