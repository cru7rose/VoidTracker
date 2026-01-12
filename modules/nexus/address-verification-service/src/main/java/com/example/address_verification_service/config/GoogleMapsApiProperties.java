// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/config/GoogleMapsApiProperties.java
package com.example.address_verification_service.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * ARCHITEKTURA: Mapowanie właściwości dla API Google Maps (Przeniesione z TES.txt).
 */
@ConfigurationProperties(prefix = "google.maps.api")
@Getter
@Setter
@Validated
public class GoogleMapsApiProperties {

    @NotBlank(message = "Google Maps API Key cannot be blank")
    private String key;

    @NotBlank(message = "Google Maps API URL cannot be blank")
    private String url;

    @NotNull(message = "Min score for Google Maps address validation cannot be null")
    @DecimalMin(value = "0.0", message = "Min score must be non-negative")
    private Double validationMinScore = 0.7;
}