package com.example.order_service.repository;

import com.example.order_service.entity.AddressIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Address Issue persistence operations
 */
@Repository
public interface AddressIssueRepository extends JpaRepository<AddressIssueEntity, UUID> {

    /**
     * Find all open issues
     */
    List<AddressIssueEntity> findByStatus(AddressIssueEntity.IssueStatus status);

    /**
     * Find issues by type
     */
    List<AddressIssueEntity> findByIssueType(AddressIssueEntity.IssueType issueType);

    /**
     * Find issues by severity
     */
    List<AddressIssueEntity> findBySeverity(AddressIssueEntity.Severity severity);

    /**
     * Find issues for a specific address
     */
    List<AddressIssueEntity> findByAddressId(UUID addressId);

    /**
     * Find issues for a specific order
     */
    List<AddressIssueEntity> findByOrderId(UUID orderId);

    /**
     * Find issues by provider
     */
    List<AddressIssueEntity> findByProvider(String provider);

    /**
     * Find open issues created after a specific time
     */
    @Query("SELECT a FROM AddressIssueEntity a WHERE a.status = 'OPEN' AND a.occurredAt >= :since")
    List<AddressIssueEntity> findOpenIssuesSince(@Param("since") Instant since);

    /**
     * Count issues by type and status
     */
    @Query("SELECT COUNT(a) FROM AddressIssueEntity a WHERE a.issueType = :type AND a.status = :status")
    long countByTypeAndStatus(
            @Param("type") AddressIssueEntity.IssueType type,
            @Param("status") AddressIssueEntity.IssueStatus status
    );
}
