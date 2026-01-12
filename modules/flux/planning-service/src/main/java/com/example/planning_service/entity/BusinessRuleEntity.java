package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "business_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRuleEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String ruleCode; // e.g., "RULE-AETR-01"

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String ruleType; // e.g., "CAPACITY_CHECK", "TIME_WINDOW"

    @Column(nullable = false)
    private String condition; // e.g., "driveTime > 240"

    @Column(nullable = false)
    private String actionExpression; // e.g., "requireBreak(45)"

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(columnDefinition = "TEXT")
    private String appliesToJson; // JSON array of tags/regions

    public enum Severity {
        INFO, WARNING, ERROR
    }
}
