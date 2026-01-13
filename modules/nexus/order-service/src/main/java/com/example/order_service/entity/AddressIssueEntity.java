package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Address Issue Entity - Tracks address verification and normalization problems.
 * Used for monitoring and resolving address-related issues.
 */
@Entity
@Table(name = "address_issue")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressIssueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Type of issue (TES_TIMEOUT, NORMALIZATION_FAILURE, CONFLICT_DETECTED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false)
    private IssueType issueType;

    /**
     * Severity level (LOW, MEDIUM, HIGH, CRITICAL)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private Severity severity;

    /**
     * Related address ID (if available)
     */
    @Column(name = "address_id")
    private UUID addressId;

    /**
     * Related order ID (if issue occurred during order processing)
     */
    @Column(name = "order_id")
    private UUID orderId;

    /**
     * Original address data (JSON string for flexibility)
     */
    @Column(name = "original_address", columnDefinition = "TEXT")
    private String originalAddress;

    /**
     * Error message or description
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Provider that failed (TES, Google Maps, Nominatim, etc.)
     */
    @Column(name = "provider")
    private String provider;

    /**
     * Issue status (OPEN, RESOLVED, IGNORED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private IssueStatus status = IssueStatus.OPEN;

    /**
     * When the issue occurred
     */
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /**
     * When the issue was resolved (if applicable)
     */
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /**
     * Resolution notes
     */
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    /**
     * Who resolved the issue (user ID or system)
     */
    @Column(name = "resolved_by")
    private String resolvedBy;

    @PrePersist
    protected void onCreate() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        if (status == null) {
            status = IssueStatus.OPEN;
        }
    }

    public enum IssueType {
        TES_TIMEOUT,
        NORMALIZATION_FAILURE,
        CONFLICT_DETECTED,
        GEOCODING_FAILED,
        VALIDATION_ERROR,
        PROVIDER_ERROR
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum IssueStatus {
        OPEN,
        RESOLVED,
        IGNORED
    }
}
