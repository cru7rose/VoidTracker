package com.example.address_verification_service.dto.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ARCHITEKTURA: Kanoniczne DTO reprezentujące pojedynczą sugestię adresową
 * od dostawcy zewnętrznego. Zapewnia ujednolicony kontrakt
 * niezależny od dostawcy (Google, HERE, Nominatim). (Przeniesione z TES.txt [cite: 473]).
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