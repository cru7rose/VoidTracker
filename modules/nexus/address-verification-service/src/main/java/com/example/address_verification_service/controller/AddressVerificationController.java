// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/controller/AddressVerificationController.java
package com.example.address_verification_service.controller;

// POPRAWKA: Ujednolicono import, aby wskazywał na moduł 'danxils-commons'.
import com.example.danxils_commons.dto.ProviderVerificationResultDTO;
import com.example.address_verification_service.dto.VerificationRequest;
import com.example.address_verification_service.service.AddressVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ARCHITEKTURA: Kontroler REST dla synchronicznej weryfikacji adresów.
 * Jest to wewnętrzne API, dostępne tylko dla zaufanych serwisów (np. admin-panel-service).
 * Zabezpieczone rolami ROLE_ADMIN i ROLE_SUPER_USER.
 */
@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
@Slf4j
public class AddressVerificationController {

    private final AddressVerificationService verificationService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_USER')")
    @PostMapping("/suggest-on-demand")
    public ResponseEntity<ProviderVerificationResultDTO> getOnDemandSuggestions(
            @RequestBody Map<String, String> addressQuery) {

        VerificationRequest request = new VerificationRequest(
                "SUGGEST_ON_DEMAND",
                addressQuery,
                null
        );
        ProviderVerificationResultDTO result = verificationService.processVerification(request);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_USER')")
    @PostMapping("/search-by-name")
    public ResponseEntity<ProviderVerificationResultDTO> searchByName(
            @RequestBody String searchQuery) {

        VerificationRequest request = new VerificationRequest(
                "SEARCH_BY_NAME",
                null,
                searchQuery
        );

        ProviderVerificationResultDTO result = verificationService.processVerification(request);
        return ResponseEntity.ok(result);
    }
}