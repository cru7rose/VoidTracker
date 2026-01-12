package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "planning_postal_code_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostalCodeRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "zone_id", nullable = false)
    private ZoneDefinitionEntity zone;

    @Column(nullable = false)
    private String postalCodeStart; // e.g., "00-000"

    @Column(nullable = false)
    private String postalCodeEnd;   // e.g., "00-999"

    @Column(nullable = false)
    private Integer priority; // Higher wins
}
