package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "planning_zone_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String code; // e.g., "WAW-Z1"

    @Column(nullable = false)
    private String name; // e.g., "Warszawa Centrum"

    @Column(nullable = false)
    private String countryCode; // e.g., "PL"

    private String description;
}
