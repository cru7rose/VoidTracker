// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/service/ConfigurationService.java
package com.example.address_verification_service.service;

import com.example.address_verification_service.service.provider.AddressVerificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Serwis do zarządzania dynamiczną konfiguracją aplikacji.
 * Przechowuje stan w pamięci (AtomicReference), co pozwala na jego zmianę w czasie rzeczywistym
 * przez API administracyjne bez konieczności restartu aplikacji.
 */
@Service
@Slf4j
public class ConfigurationService {

    private final AtomicReference<String> activeProviderName;
    private final List<String> availableProviderNames;

    public ConfigurationService(
            @Value("${address.verification.active-provider:NOMINATIM}") String defaultProvider,
            List<AddressVerificationProvider> providers) {
        this.activeProviderName = new AtomicReference<>(defaultProvider);
        this.availableProviderNames = providers.stream()
                .map(AddressVerificationProvider::getProviderName)
                .collect(Collectors.toList());
        log.info("ConfigurationService initialized. Default active provider: {}. Available providers: {}", defaultProvider, availableProviderNames);
    }

    public String getActiveProviderName() {
        return activeProviderName.get();
    }

    public void setActiveProviderName(String providerName) {
        if (providerName != null && (providerName.equalsIgnoreCase("none") || availableProviderNames.stream().anyMatch(p -> p.equalsIgnoreCase(providerName)))) {
            String oldProvider = this.activeProviderName.getAndSet(providerName);
            log.warn("Active address verification provider changed from '{}' to '{}' by administrative action.", oldProvider, providerName);
        } else {
            log.error("Attempted to set an invalid provider name: '{}'. Valid options are: {} or 'none'.", providerName, availableProviderNames);
            throw new IllegalArgumentException("Invalid provider name: " + providerName);
        }
    }

    public List<String> getAvailableProviderNames() {
        return availableProviderNames;
    }
}