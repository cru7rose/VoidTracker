package com.example.driver_app_service.service;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.danxils_commons.enums.OrderStatus;
import com.example.driver_app_service.client.OrderServiceClient;
import com.example.driver_app_service.dto.ConfirmDeliveryRequestDto;
import com.example.driver_app_service.dto.ConfirmPickupRequestDto;
import com.example.driver_app_service.dto.DriverTaskDto;
import com.example.driver_app_service.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Serwis biznesowy dla aplikacji kierowcy, oczyszczony z ręcznego
 * zarządzania tokenami. Całkowicie polega na automatycznej propagacji
 * kontekstu bezpieczeństwa przez interceptor Feign.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

    private final OrderServiceClient orderServiceClient;
    private final TaskMapper taskMapper;

    private static final List<OrderStatus> ACTIVE_TASK_STATUSES = List.of(
            OrderStatus.PICKUP,
            OrderStatus.LOAD,
            OrderStatus.TERM,
            OrderStatus.PSIP);

    public List<DriverTaskDto> getDriverTasks(String driverId) {
        log.info("Fetching active tasks for driverId: {}", driverId);
        try {
            Page<OrderResponseDto> orderPage = orderServiceClient.getOrders(
                    ACTIVE_TASK_STATUSES,
                    driverId,
                    0,
                    1000,
                    "sla,asc");
            return taskMapper.toDtoList(orderPage.getContent());
        } catch (Exception e) {
            log.error("Failed to fetch tasks for driver {}: {}", driverId, e.getMessage());
            throw new RuntimeException("Could not fetch tasks from order-service", e);
        }
    }

    public OrderResponseDto getTaskDetails(UUID orderId, String driverId) {
        log.info("Fetching details for task (order) {} for driver {}", orderId, driverId);
        OrderResponseDto order = orderServiceClient.getOrderById(orderId);

        if (order.assignedDriver() == null || !order.assignedDriver().equals(driverId)) {
            log.warn("SECURITY ALERT: Driver {} attempted to access details of order {} which is not assigned to them.",
                    driverId, orderId);
            throw new AccessDeniedException("You are not authorized to view details for this order.");
        }
        return order;
    }

    public OrderResponseDto confirmPickup(UUID orderId, String driverId, ConfirmPickupRequestDto request) {
        log.info("Confirming pickup for task (order) {} by driver {}", orderId, driverId);
        getTaskDetails(orderId, driverId);
        try {
            return orderServiceClient.confirmPickup(orderId, request);
        } catch (Exception e) {
            log.error("Failed to confirm pickup for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Could not confirm pickup via order-service", e);
        }
    }

    public OrderResponseDto confirmDelivery(UUID orderId, String driverId, ConfirmDeliveryRequestDto request) {
        log.info("Confirming delivery for task (order) {} by driver {}", orderId, driverId);
        getTaskDetails(orderId, driverId);
        try {
            return orderServiceClient.confirmDelivery(orderId, request);
        } catch (Exception e) {
            log.error("Failed to confirm delivery for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Could not confirm delivery via order-service", e);
        }
    }

    public com.example.danxils_commons.dto.ePoDDto addEpod(UUID orderId, String driverId,
            com.example.danxils_commons.dto.ePoDDto epodDto) {
        log.info("Adding ePoD for task (order) {} by driver {}", orderId, driverId);
        getTaskDetails(orderId, driverId); // Verify access
        try {
            return orderServiceClient.addEpod(orderId, epodDto);
        } catch (Exception e) {
            log.error("Failed to add ePoD for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Could not add ePoD via order-service", e);
        }
    }
}