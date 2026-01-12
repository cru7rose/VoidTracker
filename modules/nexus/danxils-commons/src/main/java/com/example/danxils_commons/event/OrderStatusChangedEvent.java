package com.example.danxils_commons.event;

import com.example.danxils_commons.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {
    private String orderId;
    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private String changedBy;
    private Instant timestamp;
}
