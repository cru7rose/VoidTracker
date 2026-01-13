package com.example.order_service.service;

import com.example.danxils_commons.dto.AddressDto;
import com.example.order_service.entity.AddressEntity;
import com.example.order_service.entity.AddressIssueEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Mediator service between address verification and issue logging.
 * Coordinates address verification, normalization, and issue tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressMediatorService {

    private final AddressVerificationService addressVerificationService;
    private final AddressIssueLoggerService addressIssueLoggerService;

    /**
     * Verify and normalize address with issue logging on failure
     */
    @Transactional
    public AddressDto verifyAndNormalizeWithLogging(AddressDto address, UUID orderId, UUID addressId, String provider) {
        try {
            log.info("Verifying address for order {}: {}", orderId, address);
            
            AddressDto verified = addressVerificationService.verifyAndNormalize(address);
            
            // Check if verification failed (no results or error)
            if (verified == null || (address.getStreet() != null && verified.getStreet() == null)) {
                String originalAddress = formatAddress(address);
                addressIssueLoggerService.logNormalizationFailure(
                        orderId,
                        addressId,
                        originalAddress,
                        "Address verification returned null or empty result",
                        provider
                );
                return address; // Return original on failure
            }
            
            log.info("✅ Address verified successfully for order {}", orderId);
            return verified;
            
        } catch (Exception e) {
            log.error("Error during address verification for order {}: {}", orderId, e.getMessage(), e);
            
            String originalAddress = formatAddress(address);
            String errorMessage = e.getMessage();
            
            // Check for timeout by checking error message (since TimeoutException may not be available)
            if (errorMessage != null && (errorMessage.toLowerCase().contains("timeout") || 
                                         errorMessage.toLowerCase().contains("timed out") ||
                                         (e.getCause() != null && e.getCause().getMessage() != null && 
                                          e.getCause().getMessage().toLowerCase().contains("timeout")))) {
                addressIssueLoggerService.logTesTimeout(
                        orderId,
                        addressId,
                        originalAddress,
                        provider,
                        10 // Default timeout
                );
            } else {
                addressIssueLoggerService.logNormalizationFailure(
                        orderId,
                        addressId,
                        originalAddress,
                        errorMessage != null ? errorMessage : "Unknown error during verification",
                        provider
                );
            }
            
            return address; // Return original on error
        }
    }

    /**
     * Verify address with timeout handling and issue logging
     */
    @Transactional
    public AddressDto verifyWithTimeoutHandling(AddressDto address, UUID orderId, UUID addressId, String provider, long timeoutSeconds) {
        try {
            log.info("Verifying address with {}s timeout for order {}: {}", timeoutSeconds, orderId, address);
            
            AddressDto verified = addressVerificationService.verifyAndNormalize(address);
            
            if (verified == null) {
                String originalAddress = formatAddress(address);
                addressIssueLoggerService.logNormalizationFailure(
                        orderId,
                        addressId,
                        originalAddress,
                        "Verification returned null result",
                        provider
                );
                return address;
            }
            
            return verified;
            
        } catch (Exception e) {
            log.error("Error during address verification for order {}: {}", orderId, e.getMessage(), e);
            String originalAddress = formatAddress(address);
            
            // Check if it's a timeout-related exception
            if (e.getMessage() != null && (e.getMessage().contains("timeout") || e.getMessage().contains("Timeout"))) {
                addressIssueLoggerService.logTesTimeout(orderId, addressId, originalAddress, provider, timeoutSeconds);
            } else {
                addressIssueLoggerService.logProviderError(
                        orderId,
                        addressId,
                        originalAddress,
                        e.getMessage(),
                        provider,
                        AddressIssueEntity.Severity.MEDIUM
                );
            }
            return address;
        }
    }

    /**
     * Check for address conflicts and log if found
     */
    @Transactional
    public void checkAndLogConflict(AddressEntity existingAddress, AddressEntity newAddress) {
        if (existingAddress == null || newAddress == null) {
            return;
        }
        
        // Simple conflict detection: same street + city but different coordinates
        boolean sameLocation = existingAddress.getStreet() != null && 
                              existingAddress.getStreet().equals(newAddress.getStreet()) &&
                              existingAddress.getCity() != null &&
                              existingAddress.getCity().equals(newAddress.getCity());
        
        boolean differentCoords = (existingAddress.getLat() != null && newAddress.getLat() != null) &&
                                 (Math.abs(existingAddress.getLat() - newAddress.getLat()) > 0.001 ||
                                  Math.abs(existingAddress.getLon() - newAddress.getLon()) > 0.001);
        
        if (sameLocation && differentCoords) {
            String conflictDetails = String.format(
                    "Address conflict: Same street (%s, %s) but different coordinates. " +
                    "Existing: (%.6f, %.6f), New: (%.6f, %.6f)",
                    existingAddress.getStreet(),
                    existingAddress.getCity(),
                    existingAddress.getLat(),
                    existingAddress.getLon(),
                    newAddress.getLat(),
                    newAddress.getLon()
            );
            
            addressIssueLoggerService.logConflict(
                    existingAddress.getId(),
                    formatAddressEntity(existingAddress),
                    conflictDetails
            );
        }
    }

    private String formatAddress(AddressDto address) {
        if (address == null) {
            return "null";
        }
        return String.format("%s %s, %s %s, %s",
                address.getStreet() != null ? address.getStreet() : "",
                address.getStreetNumber() != null ? address.getStreetNumber() : "",
                address.getPostalCode() != null ? address.getPostalCode() : "",
                address.getCity() != null ? address.getCity() : "",
                address.getCountry() != null ? address.getCountry() : "Polska"
        ).trim();
    }

    private String formatAddressEntity(AddressEntity address) {
        if (address == null) {
            return "null";
        }
        return String.format("%s %s, %s %s, %s",
                address.getStreet() != null ? address.getStreet() : "",
                address.getStreetNumber() != null ? address.getStreetNumber() : "",
                address.getPostalCode() != null ? address.getPostalCode() : "",
                address.getCity() != null ? address.getCity() : "",
                address.getCountry() != null ? address.getCountry() : "Polska"
        ).trim();
    }
}
