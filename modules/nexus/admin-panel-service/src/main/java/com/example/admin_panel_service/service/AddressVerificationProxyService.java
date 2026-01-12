// PLIK: admin-panel-service/src/main/java/com/example/admin_panel_service/service/AddressVerificationProxyService.java
package com.example.admin_panel_service.service;

import com.example.admin_panel_service.client.AddressVerificationClient;
import com.example.danxils_commons.dto.ProviderVerificationResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ARCHITEKTURA: Warstwa serwisowa proxy w BFF.
 * Jej jedynym zadaniem jest delegowanie wywołań do klienta Feign. Propagacja
 * kontekstu bezpieczeństwa (JWT) odbywa się automatycznie przez interceptor.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AddressVerificationProxyService {

    private final AddressVerificationClient client;

    public ProviderVerificationResultDTO fetchOnDemandSuggestions(Map<String, String> addressQuery) {
        log.info("Proxying synchronous suggestion request to Address Verification Service.");
        return client.getSuggestions(addressQuery);
    }

    public ProviderVerificationResultDTO fetchByNameSearch(String searchQuery) {
        log.info("Proxying synchronous search-by-name request to Address Verification Service.");
        return client.searchByName(searchQuery);
    }
}