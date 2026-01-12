package com.example.danxils_commons.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryConfirmedEvent {
    private String orderId;
    private String driverId;
    private Instant timestamp;
    private Double lat;
    private Double lng;
    private List<String> photos;
    private String signature;
}
