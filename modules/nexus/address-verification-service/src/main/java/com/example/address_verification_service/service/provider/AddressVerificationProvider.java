// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/service/provider/AddressVerificationProvider.java
package com.example.address_verification_service.service.provider;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * [cite_start]ARCHITEKTURA: Kontrakt dla wszystkich adapterów weryfikacji adresów (Przeniesione z TES.txt [cite: 2723]).
 * Zwraca CompletableFuture, umożliwiając asynchroniczną (nieblokującą) komunikację
 * z zewnętrznymi API (mimo synchronicznego API dla BFF, wewnętrznie jest to WebClient/Async).
 */
public interface AddressVerificationProvider {

    CompletableFuture<VerificationResult> verifyAddress(String street, String houseNumber, String postalCode, String city, String country);

    CompletableFuture<VerificationResult> verifyAddress(Map<String, String> addressFields);

    CompletableFuture<VerificationResult> searchByPlaceName(String placeQuery);

    String getProviderName();
}