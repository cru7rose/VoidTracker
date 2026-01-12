// File: analytics-service/src/main/java/com/example/analytics_service/entity/AnalyticsOrderEntry.java
package com.example.analytics_service.entity;

import com.example.danxils_commons.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Encja reprezentująca zdenormalizowany wpis analityczny. Została wzbogacona
 * o pola `lastKnown...` w celu przechowywania najnowszych informacji o lokalizacji
 * zlecenia, co umożliwia analizę w czasie zbliżonym do rzeczywistego.
 */
@Entity
@Table(name = "analytics_orders")
@Data
@NoArgsConstructor
public class AnalyticsOrderEntry {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    private String customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus currentStatus;

    // ... (istniejące pola timestamp)
    private Instant createdTimestamp;
    private Instant assignedTimestamp;
    private Instant pickupTimestamp;
    private Instant loadTimestamp;
    private Instant podTimestamp;
    private Instant slaTimestamp;

    private UUID planId;
    private Double plannedDistanceMeters;
    private Long plannedTimeMillis;

    // NOWE POLA
    private Double lastKnownLatitude;
    private Double lastKnownLongitude;
    private Instant lastLocationUpdateTimestamp;
}