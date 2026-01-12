// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/exception/ExternalServiceException.java
package com.example.address_verification_service.exception;

/**
 * ARCHITEKTURA: Niestandardowy, niekontrolowany wyjątek (RuntimeException)
 * używany do hermetyzacji błędów występujących podczas komunikacji
 * z zewnętrznymi dostawcami API (np. HERE, Google Maps, Nominatim).
 * Umożliwia to stworzenie spójnej warstwy obsługi błędów dla całego serwisu.
 */
public class ExternalServiceException extends RuntimeException {

    /**
     * Konstruktor dla błędów bez zagnieżdżonego wyjątku.
     * @param message Wiadomość opisująca błąd.
     */
    public ExternalServiceException(String message) {
        super(message);
    }

    /**
     * Konstruktor dla błędów, które opakowują inną przyczynę (exception).
     * @param message Wiadomość opisująca błąd.
     * @param cause Oryginalny wyjątek, który spowodował błąd.
     */
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}