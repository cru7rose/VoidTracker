package com.example.billing_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "billing_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CycleFrequency frequency;

    private int cycleDay; // e.g., 1 for 1st of month, 15 for 15th
    
    private LocalDate lastRunDate;
    private LocalDate nextRunDate;

    public enum CycleFrequency {
        MONTHLY, SEMI_MONTHLY, WEEKLY, AD_HOC, INSTANT
    }
}
