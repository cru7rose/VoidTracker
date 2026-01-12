package com.example.iam.controller;

import com.example.iam.dto.EmployeeResponseDto;
import com.example.iam.dto.UpdateEmployeeRequestDto;
import com.example.iam.entity.EmployeeEntity;
import com.example.iam.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/me")
    public ResponseEntity<EmployeeResponseDto> getMyProfile(@RequestHeader("X-User-Id") UUID userId) {
        EmployeeEntity employee = employeeService.getEmployeeByUserId(userId);
        return ResponseEntity.ok(mapToDto(employee));
    }

    @PostMapping("/me")
    public ResponseEntity<EmployeeResponseDto> createMyProfile(@RequestHeader("X-User-Id") UUID userId,
            @RequestBody UpdateEmployeeRequestDto request) {
        if (userId == null)
            throw new IllegalArgumentException("User ID cannot be null");
        EmployeeEntity employee = employeeService.createEmployeeProfile(userId, request);
        return ResponseEntity.ok(mapToDto(employee));
    }

    @PutMapping("/me")
    public ResponseEntity<EmployeeResponseDto> updateMyProfile(@RequestHeader("X-User-Id") UUID userId,
            @RequestBody UpdateEmployeeRequestDto request) {
        EmployeeEntity employee = employeeService.updateEmployeeProfile(userId, request);
        return ResponseEntity.ok(mapToDto(employee));
    }

    @GetMapping("/users/{userId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDto> getEmployeeByUserId(@PathVariable UUID userId) {
        EmployeeEntity employee = employeeService.getEmployeeByUserId(userId);
        return ResponseEntity.ok(mapToDto(employee));
    }

    @PostMapping("/users/{userId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDto> createEmployeeProfileForUser(@PathVariable UUID userId,
            @RequestBody UpdateEmployeeRequestDto request) {
        EmployeeEntity employee = employeeService.createEmployeeProfile(userId, request);
        return ResponseEntity.ok(mapToDto(employee));
    }

    @PutMapping("/users/{userId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDto> updateEmployeeProfileForUser(@PathVariable UUID userId,
            @RequestBody UpdateEmployeeRequestDto request) {
        EmployeeEntity employee = employeeService.updateEmployeeProfile(userId, request);
        return ResponseEntity.ok(mapToDto(employee));
    }

    private EmployeeResponseDto mapToDto(EmployeeEntity entity) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(entity.getId());
        dto.setLegacyId(entity.getLegacyId());
        dto.setDepartment(entity.getDepartment());
        dto.setJobTitle(entity.getJobTitle());
        dto.setAttributes(entity.getAttributes());
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getUserId().toString());
        }
        return dto;
    }
}
