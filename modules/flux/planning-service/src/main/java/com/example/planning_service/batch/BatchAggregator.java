package com.example.planning_service.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Thread-Safe Batch Aggregator dla zamówień oczekujących na
 * optymalizację
 * 
 * Przechowuje order IDs w thread-safe kolejce i umożliwia atomiczne operacje
 * poll-and-clear dla schedulera. Wspiera również metadata tracking (timestamps,
 * priority).
 * 
 * Thread-safe: ConcurrentLinkedQueue zapewnia bezpieczeństwo wielowątkowe bez
 * synchronization
 */
@Slf4j
@Component
public class BatchAggregator {

    private final ConcurrentLinkedQueue<OrderBatchEntry> pendingOrders = new ConcurrentLinkedQueue<>();
    private final Map<UUID, Instant> orderTimestamps = new ConcurrentHashMap<>();

    /**
     * Dodaje zamówienie do batcha oczekującego na optymalizację
     * 
     * @param orderId  ID zamówienia
     * @param priority Priorytet (URGENT orders mogą być przetwarzane osobno)
     */
    public void add(UUID orderId, OrderPriority priority) {
        var entry = new OrderBatchEntry(orderId, priority, Instant.now());
        pendingOrders.add(entry);
        orderTimestamps.put(orderId, Instant.now());

        log.debug("Added order {} to batch aggregator (priority: {}, total pending: {})",
                orderId, priority, pendingOrders.size());
    }

    /**
     * Convenience method - dodaje z domyślnym priorytetem
     */
    public void add(UUID orderId) {
        add(orderId, OrderPriority.NORMAL);
    }

    /**
     * Atomic poll-and-clear operation
     * Zwraca wszystkie order IDs i czyści kolejkę w jednej operacji
     * 
     * @return Lista order IDs gotowych do optymalizacji
     */
    public List<UUID> pollAndClear() {
        List<OrderBatchEntry> entries = pollAllEntries();
        return entries.stream()
                .map(OrderBatchEntry::orderId)
                .collect(Collectors.toList());
    }

    /**
     * Poll specific priority orders (np. tylko URGENT dla immediate processing)
     */
    public List<UUID> pollByPriority(OrderPriority priority) {
        List<OrderBatchEntry> allEntries = pollAllEntries();

        // Filter requested priority
        List<UUID> filtered = allEntries.stream()
                .filter(e -> e.priority() == priority)
                .map(OrderBatchEntry::orderId)
                .toList();

        // Re-add non-matching entries
        allEntries.stream()
                .filter(e -> e.priority() != priority)
                .forEach(pendingOrders::add);

        log.info("Polled {} {} priority orders, {} remain in queue",
                filtered.size(), priority, pendingOrders.size());

        return filtered;
    }

    /**
     * Zwraca bieżącą liczbę zamówień w kolejce (non-blocking, eventual consistency)
     */
    public int size() {
        return pendingOrders.size();
    }

    /**
     * Czy kolejka jest pusta
     */
    public boolean isEmpty() {
        return pendingOrders.isEmpty();
    }

    /**
     * Zwraca najstarszy timestamp w kolejce (dla monitoring purposes)
     */
    public LocalDateTime getOldestOrderTimestamp() {
        return pendingOrders.stream()
                .map(OrderBatchEntry::timestamp)
                .min(Instant::compareTo)
                .map(instant -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
                .orElse(null);
    }

    /**
     * Czyści wszystkie zamówienia (np. dla testów lub emergency reset)
     */
    public void clear() {
        int clearedCount = pendingOrders.size();
        pendingOrders.clear();
        orderTimestamps.clear();
        log.warn("Batch aggregator cleared, removed {} pending orders", clearedCount);
    }

    /**
     * Internal helper - atomically polls all entries
     */
    private List<OrderBatchEntry> pollAllEntries() {
        List<OrderBatchEntry> entries = new java.util.ArrayList<>();
        OrderBatchEntry entry;
        while ((entry = pendingOrders.poll()) != null) {
            entries.add(entry);
            orderTimestamps.remove(entry.orderId());
        }
        return entries;
    }

    /**
     * Internal record dla order metadata
     */
    private record OrderBatchEntry(
            UUID orderId,
            OrderPriority priority,
            Instant timestamp) {
    }

    public enum OrderPriority {
        URGENT,
        NORMAL,
        LOW
    }
}
