// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/service/AddressVerificationService.java
package com.example.address_verification_service.service;

import com.example.danxils_commons.dto.InternalAddressSuggestionDTO;
import com.example.danxils_commons.dto.ProviderVerificationResultDTO;
import com.example.address_verification_service.dto.VerificationRequest;
import com.example.address_verification_service.service.provider.AddressVerificationProvider;
import com.example.address_verification_service.service.provider.VerificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Główny serwis biznesowy (Core Service).
 * Zarządza wyborem dostawcy, walidacją i mapowaniem wyników. Zapewnia synchroniczną fasadę
 * nad asynchronicznymi adapterami (CompletableFuture).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AddressVerificationService {

    private final List<AddressVerificationProvider> availableProviders;
    private final ConfigurationService configurationService;

    @Value("${address.verification.timeout-seconds:10}")
    private long providerTimeoutSeconds;

    private Optional<AddressVerificationProvider> getActiveProvider() {
        String activeProviderName = configurationService.getActiveProviderName();
        if ("none".equalsIgnoreCase(activeProviderName) || activeProviderName == null) {
            return Optional.empty();
        }
        return availableProviders.stream()
                .filter(p -> p.getProviderName().equalsIgnoreCase(activeProviderName))
                .findFirst();
    }

    public ProviderVerificationResultDTO processVerification(VerificationRequest request) {
        Optional<AddressVerificationProvider> providerOpt = getActiveProvider();
        if (providerOpt.isEmpty()) {
            return ProviderVerificationResultDTO.error(
                    "N/A",
                    "No active address verification provider configured."
            );
        }

        AddressVerificationProvider provider = providerOpt.get();
        VerificationResult internalResult;
        try {
            if ("SUGGEST_ON_DEMAND".equalsIgnoreCase(request.getOperationType())) {
                Map<String, String> query = request.getAddressQuery();
                internalResult = provider.verifyAddress(
                        query.getOrDefault("street", ""),
                        query.getOrDefault("houseNumber", ""),
                        query.getOrDefault("postalCode", ""),
                        query.getOrDefault("city", ""),
                        query.getOrDefault("country", "Polska")
                ).get(providerTimeoutSeconds, TimeUnit.SECONDS);
            } else if ("SEARCH_BY_NAME".equalsIgnoreCase(request.getOperationType())) {
                internalResult = provider.searchByPlaceName(
                        request.getSearchQuery()
                ).get(providerTimeoutSeconds, TimeUnit.SECONDS);
            } else {
                return ProviderVerificationResultDTO.error("N/A", "Unknown operation type.");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Provider '{}' failed or timed out during execution. Error: {}", provider.getProviderName(), e.getMessage());
            return ProviderVerificationResultDTO.error(
                    "Query: " + (request.getAddressQuery() != null ? request.getAddressQuery().toString() : request.getSearchQuery()),
                    "Provider communication error or timeout: " + e.getMessage()
            );
        }

        return mapToDto(internalResult);
    }

    private ProviderVerificationResultDTO mapToDto(VerificationResult providerInternalResult) {
        ProviderVerificationResultDTO.VerificationStatus dtoStatus =
                ProviderVerificationResultDTO.VerificationStatus.valueOf(providerInternalResult.getStatus().name());

        List<InternalAddressSuggestionDTO> dtoSuggestions = providerInternalResult.getSuggestions().stream()
                .map(s -> InternalAddressSuggestionDTO.builder()
                        .fullAddressLabel(s.getFullAddressLabel())
                        .street(s.getStreet())
                        .houseNumber(s.getHouseNumber())
                        .postalCode(s.getPostalCode())
                        .city(s.getCity())
                        .county(s.getCounty())
                        .state(s.getState())
                        .countryCode(s.getCountryCode())
                        .countryName(s.getCountryName())
                        .latitude(s.getLatitude())
                        .longitude(s.getLongitude())
                        .matchScore(s.getMatchScore())
                        .matchLevel(s.getMatchLevel())
                        .providerSource(s.getProviderSource())
                        .build())
                .collect(Collectors.toList());

        return ProviderVerificationResultDTO.builder()
                .status(dtoStatus)
                .suggestions(dtoSuggestions)
                .message(providerInternalResult.getMessage())
                .rawQuery(providerInternalResult.getRawQuery())
                .build();
    }
}