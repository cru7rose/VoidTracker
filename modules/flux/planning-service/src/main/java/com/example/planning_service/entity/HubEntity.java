package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "hubs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String hubCode; // Alias in legacy

    @Column(nullable = false)
    private String name;

    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String country;

    // Geo-coordinates (will be needed for optimization)
    private Double latitude;
    private Double longitude;

    // Operational constraints
    private boolean isActive;
}
