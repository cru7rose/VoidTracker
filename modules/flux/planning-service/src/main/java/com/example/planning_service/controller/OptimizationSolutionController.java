package com.example.planning_service.controller;

import com.example.planning_service.dto.OptimizationSolutionSummaryDto;
import com.example.planning_service.repository.OptimizationSolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/solutions")
@RequiredArgsConstructor
public class OptimizationSolutionController {

    private final OptimizationSolutionRepository solutionRepository;

    @GetMapping
    public ResponseEntity<Page<OptimizationSolutionSummaryDto>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(solutionRepository.findAll(pageable)
                .map(entity -> {
                    OptimizationSolutionSummaryDto dto = new OptimizationSolutionSummaryDto();
                    dto.setId(entity.getId());
                    dto.setName(entity.getName());
                    dto.setCreatedAt(entity.getCreatedAt());
                    dto.setStatus(entity.getStatus().name());
                    dto.setTotalRoutes(entity.getTotalRoutes());
                    dto.setTotalStops(entity.getTotalStops());
                    dto.setTotalDistanceMeters(entity.getTotalDistanceMeters());
                    dto.setTotalDurationSeconds(entity.getTotalDurationSeconds());
                    dto.setUnassignedOrdersCount(entity.getUnassignedOrdersCount());
                    return dto;
                }));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOne(@PathVariable UUID id) {
        return solutionRepository.findById(id)
                .map(entity -> entity.getSolutionJson()) // Return the JSON directly (VehicleRoutingSolution structure)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
