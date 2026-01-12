// PLIK: admin-panel-service/src/main/java/com/example/admin_panel_service/client/AddressVerificationClient.java
package com.example.admin_panel_service.client;

import com.example.admin_panel_service.client.fallback.AddressVerificationClientFallback;
import com.example.danxils_commons.dto.ProviderVerificationResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * ARCHITEKTURA: Klient Feign do komunikacji z nowym address-verification-service.
 * Propagacja nagłówka Authorization odbywa się automatycznie przez globalny
 * RequestInterceptor. Dodano obsługę mechanizmu fallback w przypadku awarii.
 */
@FeignClient(name = "address-verification-service", fallback = AddressVerificationClientFallback.class)
public interface AddressVerificationClient {

    @PostMapping("/api/verification/suggest-on-demand")
    ProviderVerificationResultDTO getSuggestions(@RequestBody Map<String, String> addressQuery);

    @PostMapping("/api/verification/search-by-name")
    ProviderVerificationResultDTO searchByName(@RequestBody String searchQuery);
}