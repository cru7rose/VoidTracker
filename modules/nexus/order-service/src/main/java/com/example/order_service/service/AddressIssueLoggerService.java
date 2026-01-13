package com.example.order_service.service;

import com.example.order_service.entity.AddressIssueEntity;
import com.example.order_service.repository.AddressIssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for logging address-related issues.
 * Centralized logging for TES timeouts, normalization failures, and conflicts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressIssueLoggerService {

    private final AddressIssueRepository addressIssueRepository;

    /**
     * Log a TES timeout issue
     */
    @Transactional
    public UUID logTesTimeout(UUID orderId, UUID addressId, String originalAddress, String provider, long timeoutSeconds) {
        log.warn("Logging TES timeout for order {}: timeout after {}s", orderId, timeoutSeconds);
        
        AddressIssueEntity issue = AddressIssueEntity.builder()
                .issueType(AddressIssueEntity.IssueType.TES_TIMEOUT)
                .severity(AddressIssueEntity.Severity.HIGH)
                .orderId(orderId)
                .addressId(addressId)
                .originalAddress(originalAddress)
                .errorMessage(String.format("TES provider timeout after %d seconds", timeoutSeconds))
                .provider(provider != null ? provider : "TES")
                .status(AddressIssueEntity.IssueStatus.OPEN)
                .occurredAt(Instant.now())
                .build();

        AddressIssueEntity saved = addressIssueRepository.save(issue);
        log.info("✅ Logged TES timeout issue: {}", saved.getId());
        return saved.getId();
    }

    /**
     * Log a normalization failure
     */
    @Transactional
    public UUID logNormalizationFailure(UUID orderId, UUID addressId, String originalAddress, String errorMessage, String provider) {
        log.warn("Logging normalization failure for order {}: {}", orderId, errorMessage);
        
        AddressIssueEntity issue = AddressIssueEntity.builder()
                .issueType(AddressIssueEntity.IssueType.NORMALIZATION_FAILURE)
                .severity(AddressIssueEntity.Severity.MEDIUM)
                .orderId(orderId)
                .addressId(addressId)
                .originalAddress(originalAddress)
                .errorMessage(errorMessage)
                .provider(provider != null ? provider : "UNKNOWN")
                .status(AddressIssueEntity.IssueStatus.OPEN)
                .occurredAt(Instant.now())
                .build();

        AddressIssueEntity saved = addressIssueRepository.save(issue);
        log.info("✅ Logged normalization failure issue: {}", saved.getId());
        return saved.getId();
    }

    /**
     * Log an address conflict (duplicate or conflicting data)
     */
    @Transactional
    public UUID logConflict(UUID addressId, String originalAddress, String conflictDetails) {
        log.warn("Logging address conflict for address {}: {}", addressId, conflictDetails);
        
        AddressIssueEntity issue = AddressIssueEntity.builder()
                .issueType(AddressIssueEntity.IssueType.CONFLICT_DETECTED)
                .severity(AddressIssueEntity.Severity.MEDIUM)
                .addressId(addressId)
                .originalAddress(originalAddress)
                .errorMessage(conflictDetails)
                .status(AddressIssueEntity.IssueStatus.OPEN)
                .occurredAt(Instant.now())
                .build();

        AddressIssueEntity saved = addressIssueRepository.save(issue);
        log.info("✅ Logged conflict issue: {}", saved.getId());
        return saved.getId();
    }

    /**
     * Log a geocoding failure
     */
    @Transactional
    public UUID logGeocodingFailure(UUID orderId, UUID addressId, String originalAddress, String errorMessage, String provider) {
        log.warn("Logging geocoding failure for order {}: {}", orderId, errorMessage);
        
        AddressIssueEntity issue = AddressIssueEntity.builder()
                .issueType(AddressIssueEntity.IssueType.GEOCODING_FAILED)
                .severity(AddressIssueEntity.Severity.MEDIUM)
                .orderId(orderId)
                .addressId(addressId)
                .originalAddress(originalAddress)
                .errorMessage(errorMessage)
                .provider(provider != null ? provider : "UNKNOWN")
                .status(AddressIssueEntity.IssueStatus.OPEN)
                .occurredAt(Instant.now())
                .build();

        AddressIssueEntity saved = addressIssueRepository.save(issue);
        log.info("✅ Logged geocoding failure issue: {}", saved.getId());
        return saved.getId();
    }

    /**
     * Log a generic provider error
     */
    @Transactional
    public UUID logProviderError(UUID orderId, UUID addressId, String originalAddress, String errorMessage, String provider, AddressIssueEntity.Severity severity) {
        log.warn("Logging provider error for order {} ({}): {}", orderId, provider, errorMessage);
        
        AddressIssueEntity issue = AddressIssueEntity.builder()
                .issueType(AddressIssueEntity.IssueType.PROVIDER_ERROR)
                .severity(severity != null ? severity : AddressIssueEntity.Severity.MEDIUM)
                .orderId(orderId)
                .addressId(addressId)
                .originalAddress(originalAddress)
                .errorMessage(errorMessage)
                .provider(provider != null ? provider : "UNKNOWN")
                .status(AddressIssueEntity.IssueStatus.OPEN)
                .occurredAt(Instant.now())
                .build();

        AddressIssueEntity saved = addressIssueRepository.save(issue);
        log.info("✅ Logged provider error issue: {}", saved.getId());
        return saved.getId();
    }

    /**
     * Resolve an issue
     */
    @Transactional
    public void resolveIssue(UUID issueId, String resolvedBy, String resolutionNotes) {
        AddressIssueEntity issue = addressIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));

        issue.setStatus(AddressIssueEntity.IssueStatus.RESOLVED);
        issue.setResolvedAt(Instant.now());
        issue.setResolvedBy(resolvedBy);
        issue.setResolutionNotes(resolutionNotes);

        addressIssueRepository.save(issue);
        log.info("✅ Resolved issue: {}", issueId);
    }

    /**
     * Ignore an issue
     */
    @Transactional
    public void ignoreIssue(UUID issueId, String ignoredBy) {
        AddressIssueEntity issue = addressIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));

        issue.setStatus(AddressIssueEntity.IssueStatus.IGNORED);
        issue.setResolvedAt(Instant.now());
        issue.setResolvedBy(ignoredBy);
        issue.setResolutionNotes("Issue ignored by user");

        addressIssueRepository.save(issue);
        log.info("✅ Ignored issue: {}", issueId);
    }
}
