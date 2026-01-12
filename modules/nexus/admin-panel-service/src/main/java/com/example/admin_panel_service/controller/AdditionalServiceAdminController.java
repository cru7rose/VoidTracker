package com.example.admin_panel_service.controller;

import com.example.admin_panel_service.service.AdditionalServiceAdminService;
import com.example.danxils_commons.dto.AdditionalServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
public class AdditionalServiceAdminController {

    private final AdditionalServiceAdminService adminService;

    @GetMapping
    public ResponseEntity<List<AdditionalServiceDto>> getAllServices() {
        return ResponseEntity.ok(adminService.getAllServices());
    }

    @PostMapping
    public ResponseEntity<AdditionalServiceDto> createService(@RequestBody AdditionalServiceDto serviceDto) {
        AdditionalServiceDto createdService = adminService.createService(serviceDto);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<AdditionalServiceDto> getServiceById(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(adminService.getServiceById(serviceId));
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<AdditionalServiceDto> updateService(@PathVariable UUID serviceId, @RequestBody AdditionalServiceDto serviceDto) {
        AdditionalServiceDto updatedService = adminService.updateService(serviceId, serviceDto);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID serviceId) {
        adminService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }
}