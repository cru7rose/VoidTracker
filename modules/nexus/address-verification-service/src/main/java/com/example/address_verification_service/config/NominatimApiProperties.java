// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/config/NominatimApiProperties.java
package com.example.address_verification_service.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * ARCHITEKTURA: Mapowanie właściwości dla API Nominatim (Przeniesione z TES.txt).
 */
@ConfigurationProperties(prefix = "app.nominatim.api")
@Getter
@Setter
@Validated
public class NominatimApiProperties {

    @NotBlank(message = "Nominatim API URL cannot be blank")
    private String url;

    @NotBlank(message = "Email for Nominatim API usage policy is required.")
    @Email(message = "Please provide a valid email address for Nominatim.")
    private String email;
}