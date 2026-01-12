package com.example.danxils_commons.event;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAssignedEvent {
    private String orderId;
    private String driverId;
    private String assignedBy;
    private Instant timestamp;
}
