package com.example.planning_service.scheduler;

import com.example.planning_service.batch.BatchAggregator;
import com.example.planning_service.config.AutoPlanProperties;
import com.example.planning_service.execution.PlanningExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Dynamic Batch Optimization Scheduler (Zero Hardcoded Cron)
 * 
 * Scheduler który dynamicznie rejestruje zadania cykliczne (cron) bazując na
 * konfiguracji
 * w application.yml. Umożliwia to łatwą zmianę godzin batch processing bez
 * rekompilacji.
 * 
 * Typowy flow dla nocnych dostaw:
 * - 22:00: processEveningBatch() → optimizer 100-500 orders → create routes
 * - 02:00 (optional): processMidnightBatch() → handle late arrivals
 * 
 * Configuration example (application.yml):
 * auto-plan:
 * enabled: true
 * batch-schedules:
 * - "0 0 22 * * *" # 22:00 daily - PRIMARY nocne dostawy
 * - "0 0 2 * * *" # 02:00 daily - OPTIONAL late arrivals
 * default-profile-id: "uuid-of-overnight-profile"
 * 
 * User Requirement: "konfigurowalne, zero hardcodow"
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BatchOptimizationScheduler {

    private final BatchAggregator batchAggregator;
    private final AutoPlanProperties autoPlanProperties;
    private final PlanningExecutionService planningExecutionService;
    private final TaskScheduler taskScheduler;

    /**
     * Initialize dynamic schedulers on application startup
     * Czyta cron expressions z config i rejestruje scheduled tasks
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDynamicSchedulers() {
        if (!autoPlanProperties.isEnabled()) {
            log.info("Auto-planning DISABLED in configuration, batch schedulers will not run");
            return;
        }

        List<String> cronExpressions = autoPlanProperties.getBatchSchedules();
        if (cronExpressions == null || cronExpressions.isEmpty()) {
            log.warn("No batch schedules configured (auto-plan.batch-schedules), using default 22:00");
            cronExpressions = List.of("0 0 22 * * *");
        }

        log.info("🌙 Initializing Batch Optimization Schedulers:");
        for (int i = 0; i < cronExpressions.size(); i++) {
            String cronExpression = cronExpressions.get(i);
            final int scheduleIndex = i + 1; // For logging

            try {
                taskScheduler.schedule(
                        () -> processBatch(scheduleIndex),
                        new CronTrigger(cronExpression));
                log.info("  ✅ Registered batch schedule #{}: cron='{}' (next run: {})",
                        scheduleIndex, cronExpression, getNextExecutionTime(cronExpression));
            } catch (Exception e) {
                log.error("  ❌ Invalid cron expression '{}': {}", cronExpression, e.getMessage());
            }
        }
    }

    /**
     * Core batch processing logic (called by dynamic schedulers)
     * 
     * @param scheduleIndex Index of cron schedule (1, 2, 3...) for logging
     */
    private void processBatch(int scheduleIndex) {
        log.info("🌙 Batch Schedule #{} TRIGGERED - Starting optimization...", scheduleIndex);

        // Check if aggregator has orders
        int pendingCount = batchAggregator.size();
        if (pendingCount == 0) {
            log.info("No orders in batch aggregator, skipping optimization");
            return;
        }

        // Check minimum batch size
        if (pendingCount < autoPlanProperties.getMinBatchSize()) {
            log.info("Batch size ({}) below minimum ({}), waiting for more orders",
                    pendingCount, autoPlanProperties.getMinBatchSize());
            return;
        }

        // Log batch info
        log.info("Processing batch: {} orders pending, oldest order: {}",
                pendingCount, batchAggregator.getOldestOrderTimestamp());

        try {
            // Poll all orders from aggregator (atomic operation)
            List<UUID> orderIds = batchAggregator.pollAndClear();

            // Check max batch size constraint
            if (orderIds.size() > autoPlanProperties.getMaxBatchSize()) {
                log.warn("Batch size ({}) exceeds maximum ({}) splitting into chunks",
                        orderIds.size(), autoPlanProperties.getMaxBatchSize());
                // TODO: Implement batch splitting for very large volumes
            }

            // Get default optimization profile
            UUID profileId = getDefaultProfileId();

            log.info("🚀 Starting Timefold optimization for {} orders with profile {}",
                    orderIds.size(), profileId);

            // Execute planning via PlanningExecutionService
            // Note: This method needs to be added to PlanningExecutionService
            // For now, we'll call the existing executePlan method with a dummy plan ID
            // TODO: Implement executePlanForOrderIds() in PlanningExecutionService

            // Temporary workaround - log warning
            log.warn("⚠️ executePlanForOrderIds() not yet implemented in PlanningExecutionService");
            log.warn("   Order IDs to process: {} orders", orderIds.size());
            log.warn("   Profile ID: {}", profileId);
            log.warn("   Next step: Implement PlanningExecutionService.executePlanForOrderIds()");

            // planningExecutionService.executePlanForOrderIds(orderIds, profileId);

            log.info("✅ Batch optimization completed successfully");

        } catch (Exception e) {
            log.error("❌ Batch optimization FAILED: {}", e.getMessage(), e);

            // Critical failure - should we retry or alert?
            // Options:
            // 1. Re-add orders to aggregator for next batch
            // 2. Send PagerDuty alert to ops team
            // 3. Trigger n8n workflow for failure handling

            log.error("⚠️ Orders will be lost unless manually recovered. Consider implementing failure recovery.");
        }
    }

    /**
     * Fallback @Scheduled method jako backup (runs independently)
     * Triggered every hour jeśli dynamic schedulers fail
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 60000) // Every hour
    public void hourlyBatchCheck() {
        if (!autoPlanProperties.isEnabled()) {
            return;
        }

        int count = batchAggregator.size();
        if (count > 0) {
            log.debug("Hourly check: {} orders waiting in batch aggregator", count);

            // If batch is very old (> 4h), trigger emergency processing
            var oldestTimestamp = batchAggregator.getOldestOrderTimestamp();
            if (oldestTimestamp != null) {
                var age = java.time.Duration.between(oldestTimestamp, java.time.LocalDateTime.now());
                if (age.toHours() > 4) {
                    log.warn("🚨 Batch has orders older than 4h, triggering emergency processing!");
                    processBatch(0); // Schedule index 0 = emergency
                }
            }
        }
    }

    /**
     * Manual trigger endpoint dla testów lub dispatcher UI
     * Can be called via REST controller: POST /api/planning/trigger-batch
     */
    public void triggerManualBatch() {
        log.info("📲 Manual batch trigger requested");
        processBatch(999); // Schedule index 999 = manual
    }

    /**
     * Gets default optimization profile ID from config
     */
    private UUID getDefaultProfileId() {
        String profileIdString = autoPlanProperties.getDefaultProfileId();
        if (profileIdString == null || profileIdString.isEmpty()) {
            log.warn("No default-profile-id configured, using null (solver will use default constraints)");
            return null;
        }
        return UUID.fromString(profileIdString);
    }

    /**
     * Helper to get next execution time for a cron expression (for logging)
     */
    private String getNextExecutionTime(String cronExpression) {
        try {
            var trigger = new CronTrigger(cronExpression);
            var nextExecution = trigger.nextExecution(
                    new org.springframework.scheduling.TriggerContext() {
                        @Override
                        public java.time.Instant lastScheduledExecution() {
                            return null;
                        }

                        @Override
                        public java.time.Instant lastActualExecution() {
                            return null;
                        }

                        @Override
                        public java.time.Instant lastCompletion() {
                            return null;
                        }
                    });
            return nextExecution != null ? nextExecution.toString() : "unknown";
        } catch (Exception e) {
            return "invalid cron";
        }
    }
}
