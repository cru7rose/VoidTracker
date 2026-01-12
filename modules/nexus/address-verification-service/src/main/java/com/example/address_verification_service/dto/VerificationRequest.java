// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/dto/VerificationRequest.java
package com.example.address_verification_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ARCHITEKTURA: DTO dla synchronicznego żądania weryfikacji adresu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {
    @NotBlank(message = "Operation type is required (e.g., SUGGEST_ON_DEMAND, SEARCH_BY_NAME)")
    private String operationType;

    private Map<String, String> addressQuery;

    private String searchQuery;
}