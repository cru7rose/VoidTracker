// PLIK: admin-panel-service/src/main/java/com/example/admin_panel_service/controller/AddressVerificationAdminController.java
package com.example.admin_panel_service.controller;

import com.example.danxils_commons.dto.ProviderVerificationResultDTO;
import com.example.admin_panel_service.service.AddressVerificationProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ARCHITEKTURA: Kontroler wystawiony dla frontendu (Admin UI).
 * Jest to nowy, synchroniczny endpoint.
 */
@RestController
@RequestMapping("/api/admin/address-verification")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_USER')")
public class AddressVerificationAdminController {

    private final AddressVerificationProxyService proxyService;

    @PostMapping("/suggest-on-demand")
    public ResponseEntity<ProviderVerificationResultDTO> requestOnDemandSuggestions(
            @RequestBody Map<String, String> addressQuery) {
        // Token JWT jest propagowany automatycznie przez Feign Interceptor
        return ResponseEntity.ok(proxyService.fetchOnDemandSuggestions(addressQuery));
    }

    @PostMapping("/search-by-name")
    public ResponseEntity<ProviderVerificationResultDTO> requestSearchByName(
            @RequestBody String searchQuery) {
        // Token JWT jest propagowany automatycznie
        return ResponseEntity.ok(proxyService.fetchByNameSearch(searchQuery));
    }
}