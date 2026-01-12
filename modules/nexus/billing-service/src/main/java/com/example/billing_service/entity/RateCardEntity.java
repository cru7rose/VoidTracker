package com.example.billing_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "billing_rate_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String clientId; // Link to Client in IAM/Reference

    @Column(nullable = false)
    private String currency; // e.g., EUR, PLN

    private LocalDate validFrom;
    private LocalDate validTo;

    @Builder.Default
    private boolean active = true;
}
