// File: danxils-commons/src/main/java/com/example/danxils_commons/event/DriverLocationUpdatedEvent.java
package com.example.danxils_commons.event;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Zdarzenie domenowe reprezentujące zmianę lokalizacji kierowcy.
 * Nazwa pakietu została ujednolicona z resztą zdarzeń w module commons,
 * co jest krytyczne dla poprawnej deserializacji w konsumentach Kafki.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationUpdatedEvent {
    private UUID eventId;
    private UUID orderId;
    private String driverId;
    private Double latitude;
    private Double longitude;
    private Instant timestamp;

    // Telemetry
    private Integer batteryLevel;
    private Integer signalStrength;
    private Double speed;
}