package com.example.order_service.controller;

import com.example.order_service.entity.AddressIssueEntity;
import com.example.order_service.repository.AddressIssueRepository;
import com.example.order_service.service.AddressIssueLoggerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Address Issue management
 */
@RestController
@RequestMapping("/api/address-issues")
@RequiredArgsConstructor
@Slf4j
public class AddressIssueController {

    private final AddressIssueRepository addressIssueRepository;
    private final AddressIssueLoggerService addressIssueLoggerService;

    /**
     * Get all address issues (paginated)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<Page<AddressIssueEntity>> getAllIssues(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) AddressIssueEntity.IssueStatus status,
            @RequestParam(required = false) AddressIssueEntity.IssueType type,
            @RequestParam(required = false) AddressIssueEntity.Severity severity
    ) {
        log.info("Fetching address issues: status={}, type={}, severity={}", status, type, severity);
        
        // Simple filtering - in production, use Specification
        List<AddressIssueEntity> all = addressIssueRepository.findAll();
        
        if (status != null) {
            all = all.stream().filter(i -> i.getStatus() == status).toList();
        }
        if (type != null) {
            all = all.stream().filter(i -> i.getIssueType() == type).toList();
        }
        if (severity != null) {
            all = all.stream().filter(i -> i.getSeverity() == severity).toList();
        }
        
        // Manual pagination (simplified - use Specification for production)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<AddressIssueEntity> pageContent = start < all.size() ? all.subList(start, end) : List.of();
        
        Page<AddressIssueEntity> page = new PageImpl<>(
                pageContent,
                pageable,
                all.size()
        );
        
        return ResponseEntity.ok(page);
    }

    /**
     * Get issue by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<AddressIssueEntity> getIssue(@PathVariable UUID id) {
        return addressIssueRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get issues for a specific address
     */
    @GetMapping("/address/{addressId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<AddressIssueEntity>> getIssuesForAddress(@PathVariable UUID addressId) {
        return ResponseEntity.ok(addressIssueRepository.findByAddressId(addressId));
    }

    /**
     * Get issues for a specific order
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<AddressIssueEntity>> getIssuesForOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(addressIssueRepository.findByOrderId(orderId));
    }

    /**
     * Get open issues
     */
    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<AddressIssueEntity>> getOpenIssues() {
        return ResponseEntity.ok(addressIssueRepository.findByStatus(AddressIssueEntity.IssueStatus.OPEN));
    }

    /**
     * Resolve an issue
     */
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<Void> resolveIssue(
            @PathVariable UUID id,
            @RequestBody ResolveIssueRequest request
    ) {
        log.info("Resolving issue {} by user {}", id, request.getResolvedBy());
        addressIssueLoggerService.resolveIssue(id, request.getResolvedBy(), request.getResolutionNotes());
        return ResponseEntity.ok().build();
    }

    /**
     * Ignore an issue
     */
    @PostMapping("/{id}/ignore")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<Void> ignoreIssue(
            @PathVariable UUID id,
            @RequestBody IgnoreIssueRequest request
    ) {
        log.info("Ignoring issue {} by user {}", id, request.getIgnoredBy());
        addressIssueLoggerService.ignoreIssue(id, request.getIgnoredBy());
        return ResponseEntity.ok().build();
    }

    /**
     * Get issue statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<IssueStats> getStats() {
        List<AddressIssueEntity> all = addressIssueRepository.findAll();
        
        IssueStats stats = new IssueStats();
        stats.setTotal(all.size());
        stats.setOpen(all.stream().filter(i -> i.getStatus() == AddressIssueEntity.IssueStatus.OPEN).count());
        stats.setResolved(all.stream().filter(i -> i.getStatus() == AddressIssueEntity.IssueStatus.RESOLVED).count());
        stats.setIgnored(all.stream().filter(i -> i.getStatus() == AddressIssueEntity.IssueStatus.IGNORED).count());
        stats.setCritical(all.stream().filter(i -> i.getSeverity() == AddressIssueEntity.Severity.CRITICAL).count());
        stats.setHigh(all.stream().filter(i -> i.getSeverity() == AddressIssueEntity.Severity.HIGH).count());
        
        return ResponseEntity.ok(stats);
    }

    @Data
    public static class ResolveIssueRequest {
        private String resolvedBy;
        private String resolutionNotes;
    }

    @Data
    public static class IgnoreIssueRequest {
        private String ignoredBy;
    }

    @Data
    public static class IssueStats {
        private long total;
        private long open;
        private long resolved;
        private long ignored;
        private long critical;
        private long high;
    }
}
