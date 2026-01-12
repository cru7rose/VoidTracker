package com.example.analytics_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Nowa encja przeznaczona do przechowywania pełnej, historycznej
 * ścieżki GPS dla każdego zlecenia. Każdy wpis w tej tabeli odpowiada jednemu
 * zdarzeniu `DriverLocationUpdatedEvent`. Taka struktura pozwala na efektywne
 * odpytywanie i wizualizację trasy bez obciążania głównej tabeli analitycznej.
 */
@Entity
@Table(name = "analytics_location_history")
@Data
@NoArgsConstructor
public class LocationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID orderId;

    @Column(nullable = false, updatable = false)
    private String driverId;

    @Column(nullable = false, updatable = false)
    private Double latitude;

    @Column(nullable = false, updatable = false)
    private Double longitude;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;
}