package com.example.planning_service.consumer;

import com.example.danxils_commons.event.OrderCreatedEvent;
import com.example.planning_service.batch.BatchAggregator;
import com.example.planning_service.config.AutoPlanProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ARCHITEKTURA: Event-Driven Order Aggregation Consumer
 * 
 * Nasłuchuje na Kafka topic "orders.created" i dodaje nowe zamówienia do batcha
 * oczekującego na optymalizację. W przeciwieństwie do `PlanningConsumer` (który
 * prawdopodobnie robi immediate planning), ten consumer agreguje zamówienia dla
 * scheduled batch processing (np. o 22:00).
 * 
 * Flow:
 * 20:00-22:00: Orders arrive → Consumer adds to BatchAggregator
 * 22:00: BatchOptimizationScheduler polls batch → Timefold optimization
 * 22:00-23:00: Routes created, manifests published
 * 23:00: Vehicles depart
 * 
 * User Requirement: "pełna automatyzacja" + "batch o 22:00 z zero hardcoded"
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final BatchAggregator batchAggregator;
    private final AutoPlanProperties autoPlanProperties;

    /**
     * Konsumuje OrderCreatedEvent i dodaje do batcha jeśli auto-planning włączony
     * 
     * Używa dedykowanego consumer group aby nie konkurować z PlanningConsumer
     */
    @KafkaListener(topics = "orders-created", groupId = "planning-service-auto-batch", containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Check if auto-planning enabled
        if (!autoPlanProperties.isEnabled()) {
            log.trace("Auto-planning disabled, skipping order {}", event.getOrderId());
            return;
        }

        log.info("📦 Order created event received: {} (auto-planning enabled, adding to batch)",
                event.getOrderId());

        // Determine priority from event
        BatchAggregator.OrderPriority priority = determinePriority(event);

        // Add to batch aggregator
        UUID orderUuid = UUID.fromString(event.getOrderId());
        batchAggregator.add(orderUuid, priority);

        // Log batch statistics
        if (log.isDebugEnabled()) {
            log.debug("Batch aggregator status: {} orders pending, oldest order: {}",
                    batchAggregator.size(),
                    batchAggregator.getOldestOrderTimestamp());
        }

        // Check if urgent orders should trigger immediate processing
        if (priority == BatchAggregator.OrderPriority.URGENT
                && autoPlanProperties.getUrgent().isAutoReoptimize()) {

            log.warn("🚨 URGENT order {} detected! Consider triggering immediate re-optimization",
                    event.getOrderId());

            // TODO: Implement urgent re-optimization trigger
            // Could call IncrementalOptimizationService.handleUrgentOrder(event)
        }
    }

    /**
     * Determines order priority from event metadata
     * 
     * Business logic:
     * - URGENT: Emergency breakdowns, VIP customers, express delivery
     * - NORMAL: Standard dealer/technician orders
     * - LOW: Non-time-sensitive orders
     */
    private BatchAggregator.OrderPriority determinePriority(OrderCreatedEvent event) {
        // TODO: Implement priority detection logic based on:
        // - event.getPriority() (if exists in OrderCreatedEvent)
        // - event.getDeliveryType() (EMERGENCY, EXPRESS, STANDARD)
        // - event.getCustomerTier() (VIP, STANDARD)
        // - event.getTimeWindow() (if maxEndTime < 07:00 → URGENT for technicians)

        // For now, default to NORMAL
        // Priority boost logic can be implemented in n8n workflow as well
        return BatchAggregator.OrderPriority.NORMAL;
    }

    /**
     * Error handling - jeśli consumer fails, Kafka retry logic zadzia ła
     * Dla persistent failures, event pójdzie do DLT (Dead Letter Topic)
     */
    // @KafkaListener(topics = "${app.kafka.topics.orders-created-dlt}")
    // public void handleDlqMessage(OrderCreatedEvent event) {
    // log.error("Order {} failed multiple times, manual intervention required",
    // event.getOrderId());
    // // Send alert to ops team via n8n
    // }
}
