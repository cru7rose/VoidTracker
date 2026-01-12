// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/service/provider/NominatimAddressVerificationService.java
package com.example.address_verification_service.service.provider;

import com.example.address_verification_service.config.NominatimApiProperties;
import com.example.danxils_commons.dto.InternalAddressSuggestionDTO;
import com.example.address_verification_service.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * ARCHITEKTURA: Adapter do weryfikacji adresów przez Nominatim OpenStreetMap (Przeniesione z TES.txt).
 * Używa WebClient do asynchronicznych wywołań i Resilience4j do obsługi błędów sieciowych.
 */
@Service("nominatimAddressVerificationProvider")
@RequiredArgsConstructor
@Slf4j
public class NominatimAddressVerificationService implements AddressVerificationProvider {

    private final WebClient.Builder webClientBuilder;
    private final NominatimApiProperties nominatimApiProperties;
    private static final String PROVIDER_NAME = "NOMINATIM";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    @CircuitBreaker(name = "nominatimApi", fallbackMethod = "verifyAddressFallback")
    @Retry(name = "nominatimApi")
    @TimeLimiter(name = "nominatimApi")
    public CompletableFuture<VerificationResult> verifyAddress(String street, String houseNumber, String postalCode, String city, String country) {
        Map<String, String> addressFields = Map.of(
                "street", street,
                "houseNumber", houseNumber,
                "postalCode", postalCode,
                "city", city,
                "country", country
        );
        return verifyAddress(addressFields);
    }

    @Override
    @CircuitBreaker(name = "nominatimApi", fallbackMethod = "verifyAddressMapFallback")
    @Retry(name = "nominatimApi")
    @TimeLimiter(name = "nominatimApi")
    public CompletableFuture<VerificationResult> verifyAddress(Map<String, String> addressFields) {
        String freeFormQuery = buildQueryString(addressFields);
        URI uri = buildFreeFormUri(freeFormQuery);
        log.info("[{}] Weryfikacja adresu (free-form): '{}'. Wysyłanie żądania do URI: {}", getProviderName(), freeFormQuery, uri.toString());
        WebClient client = webClientBuilder.build();

        return client.get()
                .uri(uri)
                .header(HttpHeaders.USER_AGENT, buildUserAgent())
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.error("[{}] Błąd API Nominatim {}: URI: {} | BODY (truncated): {}", getProviderName(), response.statusCode(), uri, truncate(body, 500));
                            return Mono.error(new ExternalServiceException(
                                    String.format("[%s] API Error %s for query '%s'. Details: %s", getProviderName(), response.statusCode(), freeFormQuery, truncate(body, 200))));
                        })
                )
                .bodyToMono(JsonNode.class)
                .map(json -> mapToResult(json, freeFormQuery))
                .toFuture()
                .exceptionally(ex -> {
                    log.error("[{}] Wyjątek podczas komunikacji z Nominatim API dla zapytania '{}': {}", getProviderName(), freeFormQuery, ex.getMessage(), ex);
                    if (ex.getCause() instanceof ExternalServiceException) {
                        return VerificationResult.error(freeFormQuery, ex.getCause().getMessage());
                    }
                    return VerificationResult.error(freeFormQuery, "Błąd komunikacji z " + getProviderName() + ": " + ex.getMessage());
                });
    }

    @Override
    @CircuitBreaker(name = "nominatimApi", fallbackMethod = "searchByPlaceNameFallback")
    @Retry(name = "nominatimApi")
    @TimeLimiter(name = "nominatimApi")
    public CompletableFuture<VerificationResult> searchByPlaceName(String placeQuery) {
        if (placeQuery == null || placeQuery.isBlank()) {
            return CompletableFuture.completedFuture(new VerificationResult(VerificationResult.VerificationStatus.VALID, List.of(), "Puste zapytanie.", placeQuery));
        }

        URI uri = buildFreeFormUri(placeQuery);
        log.info("[{}] Wyszukiwanie po nazwie: '{}'. Wysyłanie żądania do URI: {}", getProviderName(), placeQuery, uri.toString());
        WebClient client = webClientBuilder.build();

        return client.get()
                .uri(uri)
                .header(HttpHeaders.USER_AGENT, buildUserAgent())
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.error("[{}] Błąd API Nominatim {}: URI: {} | BODY: {}", getProviderName(), response.statusCode(), uri, body);
                            return Mono.error(new ExternalServiceException(
                                    String.format("[%s] API Error %s for query '%s'", getProviderName(), response.statusCode(), placeQuery)));
                        })
                )
                .bodyToMono(JsonNode.class)
                .map(json -> mapToResult(json, placeQuery))
                .toFuture()
                .exceptionally(ex -> VerificationResult.error(placeQuery, "Błąd komunikacji z " + getProviderName() + ": " + ex.getMessage()));
    }

    public CompletableFuture<VerificationResult> searchByPlaceNameFallback(String placeQuery, Throwable t) {
        log.error("[{}] API Fallback (searchByPlaceName) dla zapytania '{}': {}", getProviderName(), placeQuery, t.getMessage());
        return CompletableFuture.completedFuture(
                VerificationResult.error(placeQuery, "Błąd usługi " + getProviderName() + " (fallback): " + t.getMessage())
        );
    }

    public CompletableFuture<VerificationResult> verifyAddressMapFallback(Map<String, String> addressFields, Throwable t) {
        String rawQuery = buildQueryString(addressFields);
        log.warn("[{}] Fallback dla Nominatim API uruchomiony dla zapytania '{}'. Błąd: {}", getProviderName(), rawQuery, t.getMessage());
        return CompletableFuture.completedFuture(
                VerificationResult.error(rawQuery, "Błąd usługi " + getProviderName() + " (fallback): " + t.getMessage())
        );
    }

    public CompletableFuture<VerificationResult> verifyAddressFallback(String street, String houseNumber, String postalCode, String city, String country, Throwable t) {
        final String rawQuery = buildQueryString(Map.of("street", street, "houseNumber", houseNumber, "postalCode", postalCode, "city", city, "country", country));
        return verifyAddressMapFallback(Map.of("street", street, "houseNumber", houseNumber, "postalCode", postalCode, "city", city, "country", country), t);
    }

    private String buildQueryString(Map<String, String> addressFields) {
        String country = Optional.ofNullable(addressFields.get("country")).filter(c -> c != null && !c.isBlank()).orElse("Polska");
        return Stream.of(addressFields.get("street"), addressFields.get("houseNumber"), addressFields.get("postalCode"), addressFields.get("city"), country)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    private URI buildFreeFormUri(String query) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(nominatimApiProperties.getUrl())
                .queryParam("q", query)
                .queryParam("format", "jsonv2")
                .queryParam("addressdetails", "1")
                .queryParam("extratags", "1")
                .queryParam("namedetails", "1")
                .queryParam("accept-language", "pl,en-US;q=0.8,en;q=0.5")
                .queryParam("limit", 5);
        return builder
                .encode(StandardCharsets.UTF_8)
                .build(false)
                .toUri();
    }

    private VerificationResult mapToResult(JsonNode root, String rawQuery) {
        if (root == null || !root.isArray() || root.isEmpty()) {
            log.warn("[{}] Brak wyników od Nominatim dla zapytania: '{}'", getProviderName(), rawQuery);
            return new VerificationResult(VerificationResult.VerificationStatus.INVALID, List.of(), "Adres nie znaleziony w Nominatim (brak odpowiedzi).", rawQuery);
        }

        List<InternalAddressSuggestionDTO> suggestions = StreamSupport.stream(root.spliterator(), false)
                .map(node -> mapToInternalDto(node, rawQuery))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (suggestions.isEmpty()) {
            log.warn("[{}] Brak poprawnych sugestii po mapowaniu odpowiedzi Nominatim dla zapytania: '{}'", getProviderName(), rawQuery);
            return new VerificationResult(VerificationResult.VerificationStatus.INVALID, List.of(), "Brak przetworzonych sugestii z Nominatim (po mapowaniu).", rawQuery);
        }

        return new VerificationResult(VerificationResult.VerificationStatus.NEEDS_REVIEW, suggestions, "Znaleziono sugestie.", rawQuery);
    }

    private InternalAddressSuggestionDTO mapToInternalDto(JsonNode node, String rawQueryForLogContext) {
        if (node == null) return null;
        try {
            JsonNode addr = node.path("address");
            String type = node.path("type").asText(null);
            String category = node.path("category").asText(null);
            JsonNode importanceNode = node.path("importance");
            double importanceScore = 0.0;
            if (importanceNode.isNumber()) {
                importanceScore = importanceNode.asDouble(0.0);
            } else if (importanceNode.isTextual()) {
                try {
                    importanceScore = Double.parseDouble(importanceNode.asText("0.0"));
                } catch (NumberFormatException e) {
                    log.warn("[{}] Nie można sparsować 'importance' ('{}') jako double dla node (rawQuery: '{}').", getProviderName(), importanceNode.asText(), rawQueryForLogContext);
                }
            }
            String displayName = node.path("display_name").asText(null);
            String matchLevel = determineMatchLevel(type, category, addr);

            InternalAddressSuggestionDTO.InternalAddressSuggestionDTOBuilder builder =
                    InternalAddressSuggestionDTO.builder();

            builder.fullAddressLabel(displayName)
                    .street(addr.path("road").asText(null))
                    .houseNumber(addr.path("house_number").asText(null))
                    .postalCode(addr.path("postcode").asText(null))
                    .city(addr.path("city").asText(addr.path("town").asText(addr.path("village").asText(null))))
                    .county(addr.path("county").asText(null))
                    .state(addr.path("state").asText(null))
                    .countryCode(addr.path("country_code").asText(null) != null ? addr.path("country_code").asText().toUpperCase(Locale.ROOT) : null)
                    .countryName(addr.path("country").asText(null))
                    .latitude(node.hasNonNull("lat") ? node.path("lat").asDouble() : null)
                    .longitude(node.hasNonNull("lon") ? node.path("lon").asDouble() : null)
                    .matchScore(importanceScore)
                    .matchLevel(matchLevel)
                    .providerSource(getProviderName());
            return builder.build();
        } catch (Exception e) {
            log.error("[{}] Błąd mapowania węzła Nominatim na DTO dla zapytania '{}'. Węzeł: {}. Błąd: {}",
                    getProviderName(), rawQueryForLogContext, truncate(node.toString(), 300), e.getMessage(), e);
            return null;
        }
    }

    private String determineMatchLevel(String type, String category, JsonNode addrNode) {
        if (addrNode.hasNonNull("house_number")) return "houseNumber";
        if (type != null && List.of("building", "office", "shop", "amenity").contains(type.toLowerCase(Locale.ROOT))) return "building";
        if (category != null && List.of("building", "shop", "office", "amenity").contains(category.toLowerCase(Locale.ROOT))) return category;
        if (addrNode.hasNonNull("road")) return "street";
        if (addrNode.hasNonNull("city") || addrNode.hasNonNull("town") || addrNode.hasNonNull("village")) return "city";
        return type != null ? type.toLowerCase(Locale.ROOT) : "unknown";
    }

    private String buildUserAgent() {
        return "AddressVerificationService/1.0 (Internal Enterprise System; Contact: " + nominatimApiProperties.getEmail() + ")";
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "null";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }
}