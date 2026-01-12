// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/service/provider/HereAddressVerificationService.java
package com.example.address_verification_service.service.provider;

import com.example.address_verification_service.config.HereApiProperties;
// POPRAWKA: Ujednolicono import, aby wskazywał na moduł 'danxils-commons'.
import com.example.danxils_commons.dto.InternalAddressSuggestionDTO;
import com.example.address_verification_service.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * ARCHITEKTURA: Adapter do weryfikacji adresów przez HERE (Przeniesione z TES.txt).
 * Używa WebClient do asynchronicznych wywołań i Resilience4j.
 */
@Service("hereAddressVerificationProvider")
@RequiredArgsConstructor
@Slf4j
public class HereAddressVerificationService implements AddressVerificationProvider {

    private final WebClient.Builder webClientBuilder;
    private final HereApiProperties hereApiProperties;
    private static final String PROVIDER_NAME = "HERE";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private String mapCountryToIso(String country) {
        if (country == null) return null;
        String countryLower = country.trim().toLowerCase();
        if ("polska".equals(countryLower) || "poland".equals(countryLower)) return "POL";
        if ("niemcy".equals(countryLower) || "germany".equals(countryLower)) return "DEU";
        return null;
    }

    @Override
    @CircuitBreaker(name = "hereApi", fallbackMethod = "verifyAddressFallback")
    @Retry(name = "hereApi")
    @TimeLimiter(name = "hereApi")
    public CompletableFuture<VerificationResult> verifyAddress(String street, String houseNumber, String postalCode, String city, String country) {
        String fullAddressQuery = String.format("%s %s, %s %s",
                street != null ? street.trim() : "",
                houseNumber != null ? houseNumber.trim() : "",
                postalCode != null ? postalCode.trim() : "",
                city != null ? city.trim() : ""
        ).replaceAll(" ,", ",").replaceAll(",\\s*,", ",").trim().replaceAll("^,", "").replaceAll(",$", "");
        if (country != null && !country.trim().isEmpty()) {
            fullAddressQuery += ", " + country.trim();
        }
        fullAddressQuery = fullAddressQuery.trim();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(hereApiProperties.getUrl())
                .queryParam("q", fullAddressQuery)
                .queryParam("limit", 5)
                .queryParam("lang", "pl-PL")
                .queryParam("apiKey", hereApiProperties.getKey());
        String countryIso = mapCountryToIso(country);
        if (countryIso != null) {
            uriBuilder.queryParam("in", "countryCode:" + countryIso);
        }

        URI uri = uriBuilder.build().toUri();
        log.info("[{}] Weryfikacja adresu: Query='{}'", getProviderName(), fullAddressQuery);
        final String finalQueryForFallback = fullAddressQuery;

        return webClientBuilder.build().get()
                .uri(uri)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[{}] API Client Error: {} - URI: {} - Body: {}", getProviderName(), clientResponse.statusCode(), uri, body);
                                    String errorMessage = String.format("[%s] API Client Error: %s. Details: %s", getProviderName(), clientResponse.statusCode(), body);
                                    if (clientResponse.statusCode().value() == 401) {
                                        errorMessage = String.format("[%s] API Unauthorized (401). Check API Key. Details: %s", getProviderName(), body);
                                    }
                                    return Mono.error(new ExternalServiceException(errorMessage));
                                })
                )
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[{}] API Server Error: {} - URI: {} - Body: {}", getProviderName(), clientResponse.statusCode(), uri, body);
                                    return Mono.error(new ExternalServiceException(String.format("[%s] API Server Error: %s. Details: %s", getProviderName(), clientResponse.statusCode(), body)));
                                })
                )
                .bodyToMono(JsonNode.class)
                .map(response -> processAndMapHereApiResponse(response, finalQueryForFallback))
                .toFuture()
                .exceptionally(ex -> {
                    log.error("[{}] Błąd wykonania zapytania do API dla query '{}': {}", getProviderName(), finalQueryForFallback, ex.getMessage(), ex);
                    return VerificationResult.error(finalQueryForFallback, "Błąd komunikacji z " + getProviderName() + ": " + ex.getMessage());
                });
    }

    @Override
    public CompletableFuture<VerificationResult> verifyAddress(Map<String, String> addressFields) {
        return verifyAddress(
                addressFields.get("street"),
                addressFields.get("houseNumber"),
                addressFields.get("postalCode"),
                addressFields.get("city"),
                addressFields.get("country")
        );
    }

    @Override
    public CompletableFuture<VerificationResult> searchByPlaceName(String placeQuery) {
        log.warn("[{}] Method 'searchByPlaceName' is not implemented in detail for HERE API proxy.", getProviderName());
        String message = String.format("[%s] Service does not support search by name yet.", getProviderName());
        return CompletableFuture.completedFuture(
                new VerificationResult(VerificationResult.VerificationStatus.SERVICE_ERROR, Collections.emptyList(), message, placeQuery)
        );
    }

    private VerificationResult processAndMapHereApiResponse(JsonNode hereResponse, String rawQuery) {
        JsonNode items = hereResponse.path("items");
        // POPRAWKA: Użyto .isMissingNode() zamiast nieistniejącej metody .isMissing()
        if (items.isMissingNode() || !items.isArray() || items.isEmpty()) {
            log.warn("[{}] API: Brak wyników dla zapytania: {}", getProviderName(), rawQuery);
            return new VerificationResult(VerificationResult.VerificationStatus.INVALID, Collections.emptyList(), "Adres nie znaleziony w " + getProviderName() + " API.", rawQuery);
        }

        List<InternalAddressSuggestionDTO> suggestions = StreamSupport.stream(items.spliterator(), false)
                .map(this::mapHereItemToInternalDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (suggestions.isEmpty()) {
            log.warn("[{}] API: Brak poprawnych sugestii po mapowaniu dla zapytania: {}", getProviderName(), rawQuery);
            return new VerificationResult(VerificationResult.VerificationStatus.INVALID, Collections.emptyList(), "Brak poprawnych sugestii adresowych z " + getProviderName() + " API.", rawQuery);
        }

        InternalAddressSuggestionDTO bestSuggestion = suggestions.get(0);
        VerificationResult.VerificationStatus status;
        String message;
        double minQueryScore = hereApiProperties.getValidationMinQueryScore() != null ? hereApiProperties.getValidationMinQueryScore() : 0.7;

        if (bestSuggestion.getMatchScore() != null && bestSuggestion.getMatchScore() >= minQueryScore) {
            if ("houseNumber".equals(bestSuggestion.getMatchLevel()) || "pointAddress".equals(bestSuggestion.getMatchLevel())) {
                status = VerificationResult.VerificationStatus.VALID;
                message = "Adres zweryfikowany pomyślnie przez " + getProviderName() + ".";
                return new VerificationResult(status, List.of(bestSuggestion), message, rawQuery);
            } else {
                status = VerificationResult.VerificationStatus.NEEDS_REVIEW;
                message = "Poziom dopasowania (" + bestSuggestion.getMatchLevel() + ") przez " + getProviderName() + " niewystarczający, wymaga weryfikacji.";
            }
        } else {
            status = VerificationResult.VerificationStatus.NEEDS_REVIEW;
            message = "Niski wynik dopasowania (" + (bestSuggestion.getMatchScore() != null ? String.format("%.2f", bestSuggestion.getMatchScore()) : "brak") + ") przez " + getProviderName() + ", wymaga weryfikacji.";
        }
        return new VerificationResult(status, suggestions, message, rawQuery);
    }

    private InternalAddressSuggestionDTO mapHereItemToInternalDto(JsonNode item) {
        if (item == null) {
            return null;
        }

        JsonNode hereAddr = item.path("address");
        JsonNode position = item.path("position");
        JsonNode scoring = item.path("scoring");

        return InternalAddressSuggestionDTO.builder()
                .fullAddressLabel(hereAddr.path("label").asText(null))
                .street(hereAddr.path("street").asText(null))
                .houseNumber(hereAddr.path("houseNumber").asText(null))
                .postalCode(hereAddr.path("postalCode").asText(null))
                .city(hereAddr.path("city").asText(null))
                .county(hereAddr.path("county").asText(null))
                .state(hereAddr.path("state").asText(null))
                .countryCode(hereAddr.path("countryCode").asText(null))
                .countryName(hereAddr.path("countryName").asText(null))
                .latitude(position.path("lat").isNumber() ? position.path("lat").asDouble() : null)
                .longitude(position.path("lng").isNumber() ? position.path("lng").asDouble() : null)
                .matchScore(scoring.path("queryScore").isNumber() ? scoring.path("queryScore").asDouble() : null)
                .matchLevel(item.path("matchLevel").asText(null))
                .providerSource(getProviderName())
                .build();
    }

    public CompletableFuture<VerificationResult> verifyAddressFallback(String street, String houseNumber, String postalCode, String city, String country, Throwable t) {
        String query = String.format("%s %s, %s %s, %s", street, houseNumber, postalCode, city, country).trim().replaceAll(", $", "");
        log.error("[{}] API Fallback dla zapytania '{}': {}", getProviderName(), query, t.getMessage(), t);
        return CompletableFuture.completedFuture(
                VerificationResult.error(query, "Błąd usługi " + getProviderName() + " (fallback): " + t.getMessage())
        );
    }
}