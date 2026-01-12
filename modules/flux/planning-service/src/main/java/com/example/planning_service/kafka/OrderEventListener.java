package com.example.planning_service.kafka;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.planning_service.service.AutoSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order Event Listener - Triggers route re-optimization when orders change
 * Part of Continuous Planning loop
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final AutoSchedulerService autoScheduler;

    private static final BigDecimal GOLDEN_ORDER_THRESHOLD = new BigDecimal("1000.00");

    /**
     * Listen to order events from Kafka
     */
    @KafkaListener(topics = "order-events", groupId = "hive-mind-solver")
    public void handleOrderEvent(String eventJson) {
        log.info("[HIVE-MIND] Received order event");

        // TODO: Parse event JSON properly
        // For now, just trigger optimization

        // In real implementation:
        // 1. Parse event
        // 2. Check if ORDER_CREATED or ORDER_UPDATED
        // 3. Fetch affected orders from DB
        // 4. Trigger solver

        // Mock: trigger with empty list (replace with real data)
        // autoScheduler.triggerOptimization(orders);

        log.info("[HIVE-MIND] Event processed (mock)");
    }

    /**
     * Detect if order is "Golden Order" (high value)
     */
    private boolean isGoldenOrder(OrderResponseDto order) {
        // TODO: Implement golden order detection with EAV properties
        // For now, return false (treat all equally)
        return false;
    }

    /**
     * Manual trigger endpoint for testing
     */
    public void manualTrigger(List<OrderResponseDto> orders) {
        log.info("[HIVE-MIND] 🔥 MANUAL TRIGGER - {} orders", orders.size());

        // Check for golden orders
        long goldenCount = orders.stream()
                .filter(this::isGoldenOrder)
                .count();

        if (goldenCount > 0) {
            log.warn("[HIVE-MIND] 🌟 GOLDEN ORDERS DETECTED: {} high-value orders!", goldenCount);
        }

        autoScheduler.triggerOptimization(orders);
    }
}
