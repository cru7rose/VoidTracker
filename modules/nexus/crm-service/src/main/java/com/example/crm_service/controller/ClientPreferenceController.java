package com.example.crm_service.controller;

import com.example.crm_service.dto.ClientPreferenceDto;
import com.example.crm_service.service.ClientPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/crm/preferences")
@RequiredArgsConstructor
public class ClientPreferenceController {

    private final ClientPreferenceService service;

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientPreferenceDto> getPreferences(@PathVariable UUID clientId) {
        return ResponseEntity.ok(service.getPreferences(clientId));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ClientPreferenceDto> updatePreferences(
            @PathVariable UUID clientId,
            @RequestBody ClientPreferenceDto dto) {
        return ResponseEntity.ok(service.updatePreferences(clientId, dto));
    }
}
