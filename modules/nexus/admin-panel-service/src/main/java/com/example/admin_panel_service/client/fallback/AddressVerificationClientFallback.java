// PLIK: admin-panel-service/src/main/java/com/example/admin_panel_service/client/fallback/AddressVerificationClientFallback.java
package com.example.admin_panel_service.client.fallback;

import com.example.admin_panel_service.client.AddressVerificationClient;
import com.example.danxils_commons.dto.ProviderVerificationResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * ARCHITEKTURA: Implementacja logiki zastępczej (fallback) dla AddressVerificationClient.
 * Gdy Circuit Breaker dla `address-verification-service` jest otwarty, metody z tej klasy
 * są wywoływane, aby zapewnić kontrolowaną degradację usługi i zwrócić
 * do frontendu informację o błędzie zamiast powodować kaskadową awarię.
 */
@Slf4j
@Component
public class AddressVerificationClientFallback implements AddressVerificationClient {

    private static final String FALLBACK_MESSAGE = "Address verification service is temporarily unavailable. Please try again later.";

    @Override
    public ProviderVerificationResultDTO getSuggestions(Map<String, String> addressQuery) {
        log.error("CIRCUIT BREAKER: Fallback for getSuggestions triggered. Address-verification-service is unavailable.");
        return ProviderVerificationResultDTO.error(addressQuery.toString(), FALLBACK_MESSAGE);
    }

    @Override
    public ProviderVerificationResultDTO searchByName(String searchQuery) {
        log.error("CIRCUIT BREAKER: Fallback for searchByName triggered. Address-verification-service is unavailable.");
        return ProviderVerificationResultDTO.error(searchQuery, FALLBACK_MESSAGE);
    }
}