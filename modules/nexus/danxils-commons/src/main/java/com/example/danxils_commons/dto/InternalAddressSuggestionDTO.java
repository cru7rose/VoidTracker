// PLIK: danxils-commons/src/main/java/com/example/danxils_commons/dto/InternalAddressSuggestionDTO.java
package com.example.danxils_commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ARCHITEKTURA: Kanoniczne DTO reprezentujące pojedynczą sugestię adresową.
 * Jako część modułu commons, zapewnia ujednolicony kontrakt niezależny od dostawcy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InternalAddressSuggestionDTO {
    private String fullAddressLabel;
    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String county;
    private String state;
    private String countryCode;
    private String countryName;
    private Double latitude;
    private Double longitude;
    private Double matchScore;
    private String matchLevel;
    private String providerSource;
}