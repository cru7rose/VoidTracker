package com.example.crm_service.controller;

import com.example.crm_service.dto.CrmStatsDto;
import com.example.crm_service.service.CrmStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crm/stats")
@RequiredArgsConstructor
public class CrmStatsController {

    private final CrmStatsService statsService;

    @GetMapping("/satisfaction")
    public ResponseEntity<CrmStatsDto> getSatisfactionStats() {
        return ResponseEntity.ok(statsService.getSatisfactionStats());
    }
}
