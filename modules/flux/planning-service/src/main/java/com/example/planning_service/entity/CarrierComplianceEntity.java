package com.example.planning_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "planning_carrier_compliance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierComplianceEntity {

    @Id
    @Column(nullable = false)
    private String carrierId;

    @Column(nullable = false)
    private Boolean isInsured;

    @Column(nullable = true)
    private LocalDate insuranceExpiryDate;

    @Column(nullable = false)
    private String complianceStatus; // "COMPLIANT", "NON_COMPLIANT", "SUSPENDED"
}
