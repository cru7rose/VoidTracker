package com.example.planning_service.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ARCHITEKTURA: Prometheus Metrics dla Route Optimization Monitoring
 * 
 * Eksportuje metryki do Prometheus/Grafana dla:
 * - Optimization performance (duration, solution quality)
 * - SLA compliance (violations, projected misses)
 * - Operational status (pending orders, batch sizes)
 * - Regional performance (per-hub metrics)
 * 
 * User Requirement: "Monitoring & Alerting - Grafana, Prometheus, PagerDuty
 * integration"
 */
@Service
@Slf4j
public class OptimizationMetrics {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter optimizationSuccessCounter;
    private final Counter optimizationFailureCounter;
    private final Counter slaViolationCounter;
    private final Counter routesCreatedCounter;

    // Timers
    private final Timer optimizationDurationTimer;
    private final Timer batchProcessingTimer;

    // Gauges (real-time values)
    private final AtomicInteger unplannedOrdersCount;
    private final AtomicInteger pendingBatchSize;
    private final AtomicLong lastOptimizationDurationMs;
    private final AtomicInteger activeVehiclesCount;

    public OptimizationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.optimizationSuccessCounter = Counter.builder("optimization.success")
                .description("Total successful route optimizations")
                .tag("service", "planning")
                .register(meterRegistry);

        this.optimizationFailureCounter = Counter.builder("optimization.failure")
                .description("Total failed route optimizations")
                .tag("service", "planning")
                .register(meterRegistry);

        this.slaViolationCounter = Counter.builder("sla.violations")
                .description("Total SLA time window violations detected")
                .tag("service", "planning")
                .register(meterRegistry);

        this.routesCreatedCounter = Counter.builder("routes.created")
                .description("Total routes created")
                .tag("service", "planning")
                .register(meterRegistry);

        // Initialize timers
        this.optimizationDurationTimer = Timer.builder("optimization.duration")
                .description("Time taken for route optimization")
                .tag("service", "planning")
                .register(meterRegistry);

        this.batchProcessingTimer = Timer.builder("batch.processing.duration")
                .description("Time taken for batch processing")
                .tag("service", "planning")
                .register(meterRegistry);

        // Initialize gauges with atomic values
        this.unplannedOrdersCount = new AtomicInteger(0);
        this.pendingBatchSize = new AtomicInteger(0);
        this.lastOptimizationDurationMs = new AtomicLong(0);
        this.activeVehiclesCount = new AtomicInteger(0);

        Gauge.builder("orders.unplanned", unplannedOrdersCount, AtomicInteger::get)
                .description("Current number of unplanned orders")
                .tag("service", "planning")
                .register(meterRegistry);

        Gauge.builder("batch.pending.size", pendingBatchSize, AtomicInteger::get)
                .description("Current batch aggregator size")
                .tag("service", "planning")
                .register(meterRegistry);

        Gauge.builder("optimization.last.duration.ms", lastOptimizationDurationMs, AtomicLong::get)
                .description("Last optimization duration in milliseconds")
                .tag("service", "planning")
                .register(meterRegistry);

        Gauge.builder("vehicles.active", activeVehiclesCount, AtomicInteger::get)
                .description("Number of active/available vehicles")
                .tag("service", "planning")
                .register(meterRegistry);

        log.info("OptimizationMetrics initialized with Prometheus registry");
    }

    // ===== COUNTER INCREMENTS =====

    public void recordOptimizationSuccess() {
        optimizationSuccessCounter.increment();
    }

    public void recordOptimizationFailure() {
        optimizationFailureCounter.increment();
        log.warn("Optimization failure recorded in metrics");
    }

    public void recordSlaViolation(String violationType) {
        slaViolationCounter.increment();
        Counter.builder("sla.violations.by_type")
                .tag("type", violationType)
                .tag("service", "planning")
                .register(meterRegistry)
                .increment();
        log.warn("SLA violation recorded: type={}", violationType);
    }

    public void recordRouteCreated() {
        routesCreatedCounter.increment();
    }

    public void recordRoutesCreated(int count) {
        for (int i = 0; i < count; i++) {
            routesCreatedCounter.increment();
        }
    }

    // ===== TIMER RECORDINGS =====

    /**
     * Record optimization duration from start to end
     */
    public void recordOptimizationDuration(Duration duration) {
        optimizationDurationTimer.record(duration);
        lastOptimizationDurationMs.set(duration.toMillis());

        log.debug("Optimization duration recorded: {} ms", duration.toMillis());
    }

    /**
     * Record batch processing duration
     */
    public void recordBatchProcessingDuration(Duration duration) {
        batchProcessingTimer.record(duration);
        log.debug("Batch processing duration recorded: {} ms", duration.toMillis());
    }

    // ===== GAUGE UPDATES =====

    public void updateUnplannedOrdersCount(int count) {
        unplannedOrdersCount.set(count);
    }

    public void updatePendingBatchSize(int size) {
        pendingBatchSize.set(size);
    }

    public void updateActiveVehiclesCount(int count) {
        activeVehiclesCount.set(count);
    }

    // ===== REGIONAL METRICS (for hub-based partitioning) =====

    /**
     * Record optimization duration per region/hub
     */
    public void recordRegionalOptimizationDuration(String hubId, Duration duration) {
        Timer.builder("optimization.duration.regional")
                .tag("hub", hubId)
                .tag("service", "planning")
                .register(meterRegistry)
                .record(duration);
    }

    /**
     * Record routes created per region
     */
    public void recordRegionalRoutesCreated(String hubId, int count) {
        Counter counter = Counter.builder("routes.created.regional")
                .tag("hub", hubId)
                .tag("service", "planning")
                .register(meterRegistry);

        for (int i = 0; i < count; i++) {
            counter.increment();
        }
    }

    // ===== HELPER: Sample Timer Usage =====

    /**
     * Start a timer sample for optimization
     * Usage:
     * Timer.Sample sample = metrics.startOptimizationTimer();
     * try {
     * // ... optimization code ...
     * sample.stop(metrics.getOptimizationDurationTimer());
     * } finally { }
     */
    public Timer.Sample startOptimizationTimer() {
        return Timer.start(meterRegistry);
    }

    public Timer getOptimizationDurationTimer() {
        return optimizationDurationTimer;
    }

    public Timer getBatchProcessingTimer() {
        return batchProcessingTimer;
    }
}
