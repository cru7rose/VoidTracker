// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/controller/AdminConfigurationController.java
package com.example.address_verification_service.controller;

import com.example.address_verification_service.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ARCHITEKTURA: Wewnętrzny kontroler administracyjny dla `address-verification-service`.
 * Umożliwia dynamiczne zarządzanie konfiguracją usługi, np. zmianę aktywnego dostawcy,
 * bez konieczności restartu aplikacji. Dostęp jest ograniczony do roli SUPER_USER.
 */
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPER_USER')")
public class AdminConfigurationController {

    private final ConfigurationService configurationService;

    @GetMapping("/providers/active")
    public ResponseEntity<Map<String, String>> getActiveProvider() {
        return ResponseEntity.ok(Map.of("activeProvider", configurationService.getActiveProviderName()));
    }

    @PostMapping("/providers/active")
    public ResponseEntity<Void> setActiveProvider(@RequestBody Map<String, String> payload) {
        configurationService.setActiveProviderName(payload.get("providerName"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/providers/available")
    public ResponseEntity<List<String>> getAvailableProviders() {
        return ResponseEntity.ok(configurationService.getAvailableProviderNames());
    }
}