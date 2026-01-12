package com.example.analytics_service.service;

import com.example.danxils_commons.dto.LatestLocationDto;
import com.example.danxils_commons.dto.LocationHistoryDto;
import com.example.analytics_service.entity.AnalyticsOrderEntry;
import com.example.analytics_service.entity.LocationHistoryEntity;
import com.example.analytics_service.repository.AnalyticsOrderRepository;
import com.example.analytics_service.repository.LocationHistoryRepository;
import com.example.danxils_commons.enums.OrderStatus;
import com.example.danxils_commons.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Serwis analityczny rozbudowany o logikę przetwarzania
 * i udostępniania danych geolokalizacyjnych. Metoda processDriverLocationUpdate
 * realizuje strategię podwójnego zapisu: do tabeli historycznej i do
 * zdenormalizowanego widoku ostatniej lokalizacji.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsOrderRepository analyticsRepository;
    private final LocationHistoryRepository locationHistoryRepository;

    @Transactional
    public void processOrderCreation(OrderCreatedEvent event) {
        log.debug("Tworzenie wpisu analitycznego dla zlecenia: {}", event.getOrderId());
        AnalyticsOrderEntry entry = new AnalyticsOrderEntry();
        entry.setOrderId(UUID.fromString(event.getOrderId()));
        entry.setCustomerId(event.getCustomerId());
        entry.setCurrentStatus(OrderStatus.NEW);
        entry.setCreatedTimestamp(Instant.now());
        if (event.getDeliveryAddress() != null) {
            entry.setSlaTimestamp(event.getDeliveryAddress().getSla());
        }
        analyticsRepository.save(entry);
    }

    @Transactional
    public void processOrderStatusChange(OrderStatusChangedEvent event) {
        log.debug("Aktualizacja statusu dla zlecenia: {}", event.getOrderId());
        AnalyticsOrderEntry entry = findOrCreateEntry(event.getOrderId());
        entry.setCurrentStatus(event.getNewStatus());
        switch (event.getNewStatus()) {
            case PICKUP -> entry.setPickupTimestamp(event.getTimestamp());
            case LOAD -> entry.setLoadTimestamp(event.getTimestamp());
            case POD -> entry.setPodTimestamp(event.getTimestamp());
            case TERM -> log.debug("Zlecenie zakończone (TERM): {}", event.getOrderId());
            default -> log.debug("Status {} nie wymaga aktualizacji timestampu", event.getNewStatus());
        }
        analyticsRepository.save(entry);
    }

    @Transactional
    public void processOrderAssignment(OrderAssignedEvent event) {
        log.debug("Aktualizacja przypisania dla zlecenia: {}", event.getOrderId());
        AnalyticsOrderEntry entry = findOrCreateEntry(event.getOrderId());
        entry.setAssignedTimestamp(event.getTimestamp());
        analyticsRepository.save(entry);
    }

    @Transactional
    public void processRoutePlanning(RoutePlannedEvent event) {
        log.debug("Aktualizacja danych planowania dla planu: {}", event.getPlanId());
        List<AnalyticsOrderEntry> entries = analyticsRepository.findAllById(event.getIncludedOrderIds());
        for (AnalyticsOrderEntry entry : entries) {
            entry.setPlanId(event.getPlanId());
            entry.setPlannedDistanceMeters(event.getTotalDistanceMeters());
            entry.setPlannedTimeMillis(event.getTotalTimeMillis());
        }
        analyticsRepository.saveAll(entries);
    }

    @Transactional
    public void processDriverLocationUpdate(DriverLocationUpdatedEvent event) {
        log.debug("Aktualizacja lokalizacji dla zlecenia: {}", event.getOrderId());

        // Zapis do pełnej historii
        LocationHistoryEntity historyEntry = new LocationHistoryEntity();
        historyEntry.setOrderId(event.getOrderId());
        historyEntry.setDriverId(event.getDriverId());
        historyEntry.setLatitude(event.getLatitude());
        historyEntry.setLongitude(event.getLongitude());
        historyEntry.setTimestamp(event.getTimestamp());
        locationHistoryRepository.save(historyEntry);

        // Aktualizacja ostatniej znanej pozycji w głównym wpisie analitycznym
        analyticsRepository.findById(event.getOrderId()).ifPresent(entry -> {
            entry.setLastKnownLatitude(event.getLatitude());
            entry.setLastKnownLongitude(event.getLongitude());
            entry.setLastLocationUpdateTimestamp(event.getTimestamp());
            analyticsRepository.save(entry);
        });
    }

    @Transactional(readOnly = true)
    public List<LocationHistoryDto> getOrderLocationHistory(UUID orderId) {
        log.debug("Pobieranie historii lokalizacji dla zlecenia: {}", orderId);
        return locationHistoryRepository.findByOrderIdOrderByTimestampAsc(orderId)
                .stream()
                .map(entity -> new LocationHistoryDto(entity.getLatitude(), entity.getLongitude(),
                        entity.getTimestamp()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LatestLocationDto> getLatestLocationsForOrders(List<UUID> orderIds) {
        log.debug("Pobieranie ostatnich lokalizacji dla {} zleceń.", orderIds.size());
        return analyticsRepository.findAllByOrderIdIn(orderIds)
                .stream()
                .filter(entry -> entry.getLastKnownLatitude() != null && entry.getLastKnownLongitude() != null)
                .map(entry -> new LatestLocationDto(
                        entry.getOrderId(),
                        entry.getLastKnownLatitude(),
                        entry.getLastKnownLongitude(),
                        entry.getLastLocationUpdateTimestamp()))
                .collect(Collectors.toList());
    }

    private AnalyticsOrderEntry findOrCreateEntry(String orderId) {
        return analyticsRepository.findById(UUID.fromString(orderId))
                .orElseGet(() -> {
                    log.warn("Nie znaleziono wpisu analitycznego dla zlecenia: {}. Tworzenie nowego.", orderId);
                    AnalyticsOrderEntry newEntry = new AnalyticsOrderEntry();
                    newEntry.setOrderId(UUID.fromString(orderId));
                    return newEntry;
                });
    }
}