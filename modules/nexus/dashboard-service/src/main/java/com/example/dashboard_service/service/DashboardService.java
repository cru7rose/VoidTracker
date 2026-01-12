package com.example.dashboard_service.service;

import com.example.danxils_commons.dto.LatestLocationDto;
import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.danxils_commons.enums.OrderStatus;
import com.example.dashboard_service.client.AnalyticsServiceClient;
import com.example.dashboard_service.client.IamAppClient;
import com.example.dashboard_service.client.OrderServiceClient;
import com.example.dashboard_service.dto.AssignDriverRequestDto;
import com.example.dashboard_service.dto.DashboardDtos.*;
import com.example.dashboard_service.dto.DashboardOrderItemDto;
// import com.example.dashboard_service.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Serwis biznesowy dla dashboardu, oczyszczony z ręcznego
 * zarządzania tokenami. Całkowicie polega na automatycznej propagacji
 * kontekstu bezpieczeństwa przez interceptor Feign.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

    private final OrderServiceClient orderServiceClient;
    private final AnalyticsServiceClient analyticsServiceClient;
    private final IamAppClient iamAppClient;
    // private final DashboardMapper dashboardMapper; // Removed to bypass MapStruct
    // issues

    private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(
            OrderStatus.PICKUP,
            OrderStatus.PSIP,
            OrderStatus.LOAD,
            OrderStatus.TERM);

    public DashboardSummaryDto getDashboardSummary() {
        log.info("Fetching dashboard summary data...");
        try {
            long newOrdersCount = fetchOrderCountByStatus(List.of(OrderStatus.NEW));
            long inTransitOrdersCount = fetchOrderCountByStatus(ACTIVE_ORDER_STATUSES);
            return new DashboardSummaryDto((int) newOrdersCount, (int) inTransitOrdersCount, 0, 0);
        } catch (Exception e) {
            log.error("Failed to fetch dashboard summary: {}", e.getMessage());
            return new DashboardSummaryDto(0, 0, 0, 0);
        }
    }

    public List<DashboardOrderItemDto> getActiveOrders(String driverId, Pageable pageable) {
        log.info("Fetching active orders for page request: {} and driverId filter: {}", pageable, driverId);
        try {
            String sortString = pageable.getSort().stream()
                    .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                    .collect(Collectors.joining());

            Page<OrderResponseDto> resultPage = orderServiceClient.getOrders(
                    ACTIVE_ORDER_STATUSES,
                    driverId,
                    null,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sortString);

            List<OrderResponseDto> orders = resultPage.getContent();
            if (orders.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> driverIds = orders.stream().map(OrderResponseDto::assignedDriver).collect(Collectors.toList());
            Map<UUID, String> driverNames = getDriverNamesMap(driverIds);

            return orders.stream()
                    .map(order -> {
                        // Manual mapping instead of MapStruct to avoid ClassNotFoundException issues
                        String pickupCity = (order.pickup() != null) ? order.pickup().getStreet() : null;
                        String deliveryCity = (order.delivery() != null) ? order.delivery().city() : null;
                        java.time.Instant sla = (order.delivery() != null) ? order.delivery().sla() : null;

                        DashboardOrderItemDto dto = new DashboardOrderItemDto(
                                order.orderId(),
                                (order.status() != null) ? order.status().toString() : null,
                                (order.priority() != null) ? order.priority().toString() : null,
                                pickupCity,
                                deliveryCity,
                                order.assignedDriver(),
                                sla);

                        if (dto.assignedDriver() != null && !dto.assignedDriver().isBlank()) {
                            UUID driverUuid = UUID.fromString(dto.assignedDriver());
                            String driverName = driverNames.getOrDefault(driverUuid, dto.assignedDriver());
                            return new DashboardOrderItemDto(
                                    dto.orderId(),
                                    dto.status(),
                                    dto.priority(),
                                    dto.pickupCity(),
                                    dto.deliveryCity(),
                                    driverName,
                                    dto.sla());
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch active orders: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<ActiveDriverLocationDto> getActiveDriversLocation() {
        log.info("Fetching active drivers location data...");
        try {
            Page<OrderResponseDto> activeOrdersPage = orderServiceClient.getOrders(ACTIVE_ORDER_STATUSES, null, null, 0,
                    1000, "sla,asc");
            List<OrderResponseDto> activeOrders = activeOrdersPage.getContent();
            if (activeOrders.isEmpty()) {
                return Collections.emptyList();
            }

            Map<UUID, String> driverNames = getDriverNamesMap(
                    activeOrders.stream().map(OrderResponseDto::assignedDriver).collect(Collectors.toList()));
            List<UUID> orderIds = activeOrders.stream().map(OrderResponseDto::orderId).collect(Collectors.toList());
            List<LatestLocationDto> locations = analyticsServiceClient.getLatestLocations(orderIds);
            Map<UUID, LatestLocationDto> locationMap = locations.stream()
                    .collect(Collectors.toMap(LatestLocationDto::getOrderId, Function.identity()));

            return activeOrders.stream()
                    .filter(order -> locationMap.containsKey(order.orderId()) && order.assignedDriver() != null)
                    .map(order -> {
                        LatestLocationDto location = locationMap.get(order.orderId());
                        UUID driverId = UUID.fromString(order.assignedDriver());
                        String driverName = driverNames.getOrDefault(driverId, order.assignedDriver());
                        return new ActiveDriverLocationDto(driverId.toString(), driverName, location.getLatitude(),
                                location.getLongitude(), location.getTimestamp(), order.orderId());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch active drivers location data: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private long fetchOrderCountByStatus(List<OrderStatus> statuses) {
        try {
            Page<OrderResponseDto> resultPage = orderServiceClient.getOrders(statuses, null, null, 0, 1,
                    "created,desc");
            return resultPage.getTotalElements();
        } catch (Exception e) {
            log.error("Failed to fetch order count for statuses {}: {}", statuses, e.getMessage());
            return 0;
        }
    }

    private Map<UUID, String> getDriverNamesMap(List<String> driverIds) {
        if (driverIds == null || driverIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UUID> uniqueDriverIds = driverIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .map(UUID::fromString)
                .collect(Collectors.toList());
        if (uniqueDriverIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return iamAppClient.getUserDetails(uniqueDriverIds).stream()
                    .collect(Collectors.toMap(IamAppClient.UserDetailsDto::userId,
                            IamAppClient.UserDetailsDto::username));
        } catch (Exception e) {
            log.error("Failed to fetch user details from iam-app: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    public OrderResponseDto assignDriverToOrder(UUID orderId, AssignDriverRequestDto request) {
        log.info("Processing driver assignment for order {}", orderId);
        try {
            return orderServiceClient.assignDriver(orderId, request);
        } catch (Exception e) {
            log.error("Failed to assign driver to order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Error during driver assignment.", e);
        }
    }
}