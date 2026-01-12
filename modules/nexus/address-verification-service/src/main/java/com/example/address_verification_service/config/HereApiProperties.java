// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/config/HereApiProperties.java
package com.example.address_verification_service.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * ARCHITEKTURA: Mapowanie właściwości dla API HERE (Przeniesione z TES.txt).
 */
@ConfigurationProperties(prefix = "here.api")
@Getter
@Setter
@Validated
public class HereApiProperties {

    @NotBlank(message = "HERE API URL cannot be blank")
    private String url;

    @NotBlank(message = "HERE API Key cannot be blank")
    private String key;

    @NotNull(message = "Min query score for address validation cannot be null")
    @DecimalMin(value = "0.0", message = "Min query score must be non-negative")
    private Double validationMinQueryScore = 0.7;
}