package com.example.planning_service.monitoring;

import com.example.planning_service.config.AutoPlanProperties;
import com.example.planning_service.repository.FleetVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * ARCHITEKTURA: SLA Alert Service dla nocnych dostaw (7 AM / 8 AM deadlines)
 * 
 * Monitoruje projected delivery times i wysyła alerty:
 * - 2h przed deadline: WARNING (Slack notification via n8n)
 * - 1h przed deadline: CRITICAL (PagerDuty alert)
 * - Real-time: Każde 15 min sprawdza compliance
 * 
 * Triggers:
 * - Optimization completion: Check if routes meet SLA
 * - Periodic checks: Every 15 min during operational window (22:00-08:00)
 * - Manual trigger: Dispatcher can request SLA check
 * 
 * User Requirements:
 * - "jak najwczesniej, pelna komunikacja o czasie dostawy"
 * - "ewentualne problemy i opoznieniach musi wychodzic do klientow per email"
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SlaAlertService {

    private final AutoPlanProperties autoPlanProperties;
    private final OptimizationMetrics metrics;
    private final FleetVehicleRepository vehicleRepository;
    // TODO: Add dependencies when implemented:
    // private final N8NWebhookClient n8nClient;
    // private final PagerDutyClient pagerDutyClient;

    /**
     * Periodic SLA compliance check
     * Runs every 15 minutes during operational hours (configurable)
     */
    @Scheduled(fixedRateString = "${auto-plan.sla.check-frequency-minutes:15}000", initialDelay = 60000) // 15 min
                                                                                                         // default, 1
                                                                                                         // min initial
                                                                                                         // delay
    public void performPeriodicSlaCheck() {
        if (!autoPlanProperties.isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();

        // Only check during operational window: 22:00-08:00 (10pm-8am)
        boolean isOperationalHour = hour >= 22 || hour <= 8;
        if (!isOperationalHour) {
            log.trace("Outside operational hours ({}:00), skipping SLA check", hour);
            return;
        }

        log.info("🔍 Periodic SLA compliance check started at {}", now);

        try {
            checkSlaCompliance();
        } catch (Exception e) {
            log.error("SLA compliance check FAILED: {}", e.getMessage(), e);
        }
    }

    /**
     * Main SLA compliance check logic
     * 
     * TODO: Implement actual route checking when route entities are available
     * Currently placeholder for infrastructure
     */
    public void checkSlaCompliance() {
        LocalDateTime now = LocalDateTime.now();

        // TODO: Query active routes from database
        // List<RouteEntity> activeRoutes =
        // routeRepository.findByStatus(RouteStatus.IN_PROGRESS);

        // Placeholder logic for demonstration
        log.debug("Checking SLA compliance for active routes...");

        // Example: Check for routes approaching deadline
        LocalDateTime twoHoursFromNow = now.plusHours(2);
        LocalDateTime oneHourFromNow = now.plusHours(1);

        // Mock check - replace with actual route query
        boolean hasWarningLevel = checkForUpcomingDeadlines(twoHoursFromNow, "WARNING");
        boolean hasCriticalLevel = checkForUpcomingDeadlines(oneHourFromNow, "CRITICAL");

        if (hasWarningLevel) {
            log.warn("⚠️ WARNING: Routes approaching SLA deadline within 2 hours");
            sendSlackAlert("WARNING: Routes approaching deadline (2h buffer)");
        }

        if (hasCriticalLevel) {
            log.error("🚨 CRITICAL: Routes approaching SLA deadline within 1 hour!");
            sendPagerDutyAlert("CRITICAL: SLA violation imminent (1h buffer)");
        }
    }

    /**
     * Check for routes with deliveries approaching deadlines
     * 
     * @param deadline Target deadline to check against
     * @param level    Alert level (WARNING, CRITICAL)
     * @return true if violations found
     */
    private boolean checkForUpcomingDeadlines(LocalDateTime deadline, String level) {
        // TODO: Implement actual route/order query
        // Example query:
        // SELECT * FROM routes r
        // JOIN route_stops s ON r.id = s.route_id
        // WHERE s.estimated_arrival > s.time_window_end
        // AND s.time_window_end <= :deadline
        // AND r.status = 'IN_PROGRESS'

        // Placeholder - return false for now
        return false;
    }

    /**
     * Send Slack notification via n8n webhook
     */
    private void sendSlackAlert(String message) {
        log.info("📢 Slack Alert (via n8n): {}", message);

        // TODO: Implement n8n webhook call
        // n8nClient.triggerWorkflow("sla-warning-slack", Map.of(
        // "message", message,
        // "timestamp", LocalDateTime.now(),
        // "severity", "warning"
        // ));
    }

    /**
     * Send PagerDuty critical alert
     */
    private void sendPagerDutyAlert(String message) {
        log.error("🚨 PagerDuty Alert: {}", message);
        metrics.recordSlaViolation("CRITICAL");

        // TODO: Implement PagerDuty API call
        // pagerDutyClient.createIncident(PagerDutyIncident.builder()
        // .title("SLA Violation Imminent")
        // .description(message)
        // .severity("critical")
        // .timestamp(Instant.now())
        // .build());
    }

    /**
     * Manual SLA check trigger (e.g., from dispatcher UI or after optimization)
     */
    public SlaCheckResult performManualSlaCheck() {
        log.info("🔍 Manual SLA check triggered");

        try {
            checkSlaCompliance();
            return new SlaCheckResult(true, "SLA check completed successfully", 0);
        } catch (Exception e) {
            log.error("Manual SLA check failed: {}", e.getMessage(), e);
            return new SlaCheckResult(false, e.getMessage(), -1);
        }
    }

    /**
     * Check SLA compliance after optimization completes
     * Called by PlanningExecutionService after route creation
     */
    public void checkSlaAfterOptimization(int routesCreated, int ordersPlanned) {
        log.info("📊 Post-optimization SLA check: {} routes, {} orders", routesCreated, ordersPlanned);

        // TODO: Check if generated routes meet time windows
        // For each route:
        // - Verify last stop arrival < time window end
        // - Check if vehicle can return to depot by shift end
        // - Flag any routes with projected violations

        // Placeholder
        boolean allRoutesMeetSla = true; // TODO: actual check

        if (!allRoutesMeetSla) {
            log.warn("⚠️ Some routes may not meet SLA commitments!");
            sendSlackAlert(String.format(
                    "Optimization completed but %d/%d routes have SLA concerns",
                    0, routesCreated // TODO: actual counts
            ));
        } else {
            log.info("✅ All routes meet SLA commitments");
        }
    }

    /**
     * Send customer email notification via n8n
     * Called when route is assigned or delayed
     */
    public void sendCustomerNotification(String orderId, String notificationType, String message) {
        if (!autoPlanProperties.getSla().isCustomerEmailEnabled()) {
            log.debug("Customer email notifications disabled, skipping");
            return;
        }

        log.info("📧 Customer notification: order={}, type={}, message={}",
                orderId, notificationType, message);

        // TODO: Trigger n8n workflow for customer email
        // n8nClient.triggerWorkflow("customer-notification-email", Map.of(
        // "orderId", orderId,
        // "notificationType", notificationType,
        // "message", message,
        // "timestamp", LocalDateTime.now()
        // ));
    }

    /**
     * Result object for manual SLA checks
     */
    public record SlaCheckResult(
            boolean success,
            String message,
            int violationsFound) {
    }
}
