package com.example.order_service.controller;

import com.example.order_service.entity.SystemConfigEntity;
import com.example.order_service.entity.UserConfigEntity;
import com.example.order_service.entity.UserConfigId;
import com.example.order_service.repository.SystemConfigRepository;
import com.example.order_service.repository.UserConfigRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
@Tag(name = "Configuration Management", description = "API for system and user configurations")
public class ConfigController {

    private final SystemConfigRepository systemConfigRepository;
    private final UserConfigRepository userConfigRepository;

    @GetMapping("/system/{key}")
    @Operation(summary = "Get system configuration")
    public ResponseEntity<String> getSystemConfig(@PathVariable String key) {
        return systemConfigRepository.findById(key)
                .map(config -> ResponseEntity.ok(config.getValue()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/system/{key}")
    @Operation(summary = "Update system configuration")
    public ResponseEntity<Void> updateSystemConfig(@PathVariable String key, @RequestBody String value) {
        SystemConfigEntity config = systemConfigRepository.findById(key)
                .orElse(new SystemConfigEntity(key, value, Instant.now(), "system"));

        config.setValue(value);
        config.setUpdatedAt(Instant.now());
        // In a real app, get userId from security context
        config.setUpdatedBy("admin");

        systemConfigRepository.save(config);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/{key}")
    @Operation(summary = "Get user configuration")
    public ResponseEntity<String> getUserConfig(@PathVariable String userId, @PathVariable String key) {
        return userConfigRepository.findById(new UserConfigId(userId, key))
                .map(config -> ResponseEntity.ok(config.getValue()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/user/{userId}/{key}")
    @Operation(summary = "Update user configuration")
    public ResponseEntity<Void> updateUserConfig(@PathVariable String userId, @PathVariable String key,
            @RequestBody String value) {
        UserConfigEntity config = userConfigRepository.findById(new UserConfigId(userId, key))
                .orElse(new UserConfigEntity(userId, key, value));

        config.setValue(value);
        userConfigRepository.save(config);
        return ResponseEntity.ok().build();
    }
}
