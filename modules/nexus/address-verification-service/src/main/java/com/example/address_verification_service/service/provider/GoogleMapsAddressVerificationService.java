// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/service/provider/GoogleMapsAddressVerificationService.java
package com.example.address_verification_service.service.provider;

import com.example.address_verification_service.config.GoogleMapsApiProperties;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

/**
 * ARCHITEKTURA: Adapter do weryfikacji adresów przez Google Maps (Przeniesione z TES.txt).
 * Używa WebClient do asynchronicznych wywołań i Resilience4j.
 */
@Service("googleMapsAddressVerificationProvider")
@RequiredArgsConstructor
@Slf4j
public class GoogleMapsAddressVerificationService implements AddressVerificationProvider {

    private final GoogleMapsApiProperties googleMapsApiProperties;
    private final WebClient.Builder webClientBuilder;
    private static final String PROVIDER_NAME = "GOOGLE";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    @CircuitBreaker(name = "googleMapsApi", fallbackMethod = "verifyAddressFallback")
    @Retry(name = "googleMapsApi")
    @TimeLimiter(name = "googleMapsApi")
    public CompletableFuture<VerificationResult> verifyAddress(String street, String houseNumber, String postalCode, String city, String country) {
        String fullAddressQuery = String.format("%s %s, %s %s, %s",
                street != null ? street.trim() : "",
                houseNumber != null ? houseNumber.trim() : "",
                postalCode != null ? postalCode.trim() : "",
                city != null ? city.trim() : "",
                country != null ? country.trim() : ""
        ).replaceAll(" ,", ",").replaceAll(",\\s*,", ",").trim().replaceAll("^,", "").replaceAll(",$", "");

        URI uri = UriComponentsBuilder.fromUriString(googleMapsApiProperties.getUrl())
                .queryParam("address", fullAddressQuery)
                .queryParam("key", googleMapsApiProperties.getKey())
                .queryParam("language", "pl")
                .build(true)
                .toUri();
        log.info("[{}] Verifying address: Query='{}'", getProviderName(), fullAddressQuery);

        return webClientBuilder.build().get()
                .uri(uri)
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.error("[{}] Google API Error {}: {}", getProviderName(), response.statusCode(), body);
                            return Mono.error(new ExternalServiceException(
                                    String.format("[%s] API Error %s. Details: %s", getProviderName(), response.statusCode(), body))
                            );
                        })
                )
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> mapToVerificationResult(jsonNode, fullAddressQuery))
                .toFuture()
                .exceptionally(ex -> VerificationResult.error(fullAddressQuery, "Error communicating with Google Maps API: " + ex.getMessage()));
    }

    private VerificationResult mapToVerificationResult(JsonNode response, String rawQuery) {
        String status = response.path("status").asText();
        if (!"OK".equals(status)) {
            String errorMessage = response.path("error_message").asText("Unknown API error");
            log.warn("[{}] API returned status '{}' for query '{}'. Message: {}", getProviderName(), status, rawQuery, errorMessage);
            return new VerificationResult(VerificationResult.VerificationStatus.INVALID, Collections.emptyList(), "Address not found in Google Maps API. Status: " + status, rawQuery);
        }

        List<InternalAddressSuggestionDTO> suggestions = new ArrayList<>();
        StreamSupport.stream(response.path("results").spliterator(), false)
                .forEach(resultNode -> {
                    InternalAddressSuggestionDTO suggestion = mapJsonToSuggestion(resultNode);
                    if (suggestion != null) {
                        suggestions.add(suggestion);
                    }
                });

        if (suggestions.isEmpty()) {
            return new VerificationResult(VerificationResult.VerificationStatus.INVALID, Collections.emptyList(), "No valid suggestions from Google Maps API.", rawQuery);
        }

        return new VerificationResult(VerificationResult.VerificationStatus.NEEDS_REVIEW, suggestions, "Suggestions found in Google Maps API.", rawQuery);
    }

    private InternalAddressSuggestionDTO mapJsonToSuggestion(JsonNode resultNode) {
        if (resultNode == null) return null;
        JsonNode location = resultNode.path("geometry").path("location");
        InternalAddressSuggestionDTO.InternalAddressSuggestionDTOBuilder builder = InternalAddressSuggestionDTO.builder();

        builder.fullAddressLabel(resultNode.path("formatted_address").asText(null))
                .latitude(location.has("lat") ? location.get("lat").asDouble() : null)
                .longitude(location.has("lng") ? location.get("lng").asDouble() : null)
                .providerSource(PROVIDER_NAME);

        String streetNumber = "";
        String route = "";

        for (JsonNode component : resultNode.path("address_components")) {
            String type = component.path("types").get(0).asText("");
            String longName = component.path("long_name").asText(null);
            String shortName = component.path("short_name").asText(null);

            switch (type) {
                case "street_number":
                    streetNumber = longName;
                    break;
                case "route":
                    route = longName;
                    break;
                case "postal_code":
                    builder.postalCode(longName);
                    break;
                case "locality":
                    builder.city(longName);
                    break;
                case "country":
                    builder.countryName(longName).countryCode(shortName);
                    break;
            }
        }

        builder.street(route);
        builder.houseNumber(streetNumber);
        return builder.build();
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
        log.warn("[{}] Method 'searchByPlaceName' is not implemented in detail for Google Maps API proxy.", getProviderName());
        return CompletableFuture.completedFuture(
                VerificationResult.error(placeQuery, "Function not implemented for Google Maps.")
        );
    }

    public CompletableFuture<VerificationResult> verifyAddressFallback(String street, String houseNumber, String postalCode, String city, String country, Throwable t) {
        String query = String.format("%s %s, %s %s, %s", street, houseNumber, postalCode, city, country);
        log.error("[{}] API Fallback for query '{}': {}", getProviderName(), query, t.getMessage());
        return CompletableFuture.completedFuture(
                VerificationResult.error(query, "Error with service " + getProviderName() + " (fallback): " + t.getMessage())
        );
    }
}