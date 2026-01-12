package com.example.planning_service.controller;

import com.example.planning_service.entity.MilkrunTemplateEntity;
import com.example.planning_service.repository.MilkrunTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/milkruns")
@RequiredArgsConstructor
public class MilkrunController {

    private final MilkrunTemplateRepository repository;

    @GetMapping
    public ResponseEntity<List<MilkrunTemplateEntity>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    public ResponseEntity<MilkrunTemplateEntity> create(@RequestBody MilkrunTemplateEntity entity) {
        // Simple validation or defaults
        if (entity.getStops() == null) {
            entity.setStops(List.of());
        }
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MilkrunTemplateEntity> update(@PathVariable UUID id,
            @RequestBody MilkrunTemplateEntity entity) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(entity.getName());
                    existing.setDriverId(entity.getDriverId());
                    existing.setVehicleId(entity.getVehicleId());
                    existing.setSchedule(entity.getSchedule());
                    existing.setStops(entity.getStops());
                    existing.setActive(entity.isActive());
                    return ResponseEntity.ok(repository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
