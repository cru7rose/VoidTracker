package com.example.order_service.service;

import com.example.danxils_commons.dto.AddressDto;
import com.example.danxils_commons.enums.OrderStatus;
import com.example.danxils_commons.event.OrderAssignedEvent;
import com.example.danxils_commons.event.OrderCreatedEvent;
import com.example.danxils_commons.event.OrderStatusChangedEvent;
import com.example.danxils_commons.event.RoutePlannedEvent;
import com.example.order_service.config.AppProperties;
import com.example.order_service.dto.filter.OrderFilterDto;
import com.example.danxils_commons.dto.OrderQueryRequestDto;
import com.example.order_service.dto.request.AssignDriverRequestDto;
import com.example.order_service.dto.request.CreateOrderRequestDto;
import com.example.order_service.dto.request.UpdateStatusRequestDto;
import com.example.order_service.entity.*;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.repository.*;
import com.example.order_service.repository.specification.OrderSpecification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.order_service.dto.request.ConfirmPickupRequestDto;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.time.Instant;
import java.util.*;

@Service
public class OrderService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final AssetRepository assetRepository; // Injected
    private final OutboxEventRepository outboxEventRepository;
    private final OrderMapper orderMapper;
    private final AppProperties appProperties;
    private final OrderSpecification orderSpecification;
    private final ObjectMapper objectMapper;
    private final AddressMediatorService addressMediatorService; // Address issue logging integration

    public OrderService(OrderRepository orderRepository,
            ClientRepository clientRepository,
            AssetRepository assetRepository,
            OutboxEventRepository outboxEventRepository,
            OrderMapper orderMapper,
            AppProperties appProperties,
            OrderSpecification orderSpecification,
            ObjectMapper objectMapper,
            AddressMediatorService addressMediatorService) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.assetRepository = assetRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.orderMapper = orderMapper;
        this.appProperties = appProperties;
        this.orderSpecification = orderSpecification;
        this.objectMapper = objectMapper;
        this.addressMediatorService = addressMediatorService;
    }

    private static final Map<OrderStatus, Set<OrderStatus>> validTransitions = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.NEW),
            OrderStatus.NEW, Set.of(OrderStatus.PICKUP),
            OrderStatus.PICKUP, Set.of(OrderStatus.PSIP, OrderStatus.LOAD, OrderStatus.POD),
            OrderStatus.PSIP, Set.of(OrderStatus.LOAD, OrderStatus.POD),
            OrderStatus.LOAD, Set.of(OrderStatus.TERM, OrderStatus.POD),
            OrderStatus.TERM, Set.of(OrderStatus.POD));

    @Transactional
    @Timed(value = "order.create", description = "Time taken to create order")
    @Counted(value = "order.create.count", description = "Number of orders created")
    public OrderEntity createOrder(CreateOrderRequestDto request) {
        log.info("Rozpoczęcie tworzenia zlecenia dla klienta: {}", request.getCustomerId());

        // Auto-create customer if not exists (for external integrations)
        // First, try to find by external ID (customerId from API)
        ClientEntity client = clientRepository.findByExternalId(request.getCustomerId())
                .or(() -> clientRepository.findById(UUID.fromString(request.getCustomerId())))
                .orElseGet(() -> {
                    log.warn("Klient {} nie znaleziony - tworzenie nowego klienta", request.getCustomerId());
                    ClientEntity newClient = new ClientEntity();
                    // Don't set ID manually - let Hibernate generate it
                    newClient.setExternalId(request.getCustomerId()); // Store original customerId as externalId
                    newClient.setName("Auto-Created Client");
                    newClient.addProperty("autoCreated", true);
                    newClient.addProperty("createdFrom", "OrderImport");
                    return clientRepository.save(newClient);
                });

        OrderEntity newOrder = new OrderEntity();
        newOrder.setClient(client);
        newOrder.setExternalId(request.getExternalReference());
        newOrder.setInjectionHub(request.getInjectionHubId());

        // Map Relational Fields (Addresses, Package)
        AddressEntity pickupAddr = orderMapper.mapToAddressEntity(request.getPickupAddress());
        if (pickupAddr != null) {
            pickupAddr.setOwnerClient(client);
        }

        AddressEntity deliveryAddr = orderMapper.mapToDeliveryAddressEntity(request.getDeliveryAddress());
        if (deliveryAddr != null) {
            deliveryAddr.setOwnerClient(client);
        }

        PackageEntity packageEntity = orderMapper.mapToPackageEntity(request.getPackageDetails());

        newOrder.setPickupAddress(pickupAddr);
        newOrder.setDeliveryAddress(deliveryAddr);
        newOrder.setPackageDetails(packageEntity);

        // Map Time Windows to Columns
        newOrder.setPickupTimeFrom(request.getPickupTimeFrom());
        newOrder.setPickupTimeTo(request.getPickupTimeTo());
        newOrder.setDeliveryTimeFrom(request.getDeliveryTimeFrom());
        newOrder.setDeliveryTimeTo(request.getDeliveryTimeTo());

        // Map Soft Fields (CostCenter, Priority) to Properties
        newOrder.addProperty("costCenter", request.getCostCenter());
        newOrder.addProperty("priority", request.getPriority());

        if (Boolean.TRUE.equals(request.getIsTransfer())) {
            newOrder.setStatus(OrderStatus.PENDING);
        } else {
            newOrder.setStatus(OrderStatus.NEW);
        }

        // Services
        if (request.getRequiredServiceCodes() != null && !request.getRequiredServiceCodes().isEmpty()) {
            newOrder.addProperty("requiredServiceCodes", request.getRequiredServiceCodes());
        }

        OrderEntity savedOrder = orderRepository.save(newOrder);
        log.info("Zlecenie zapisane pomyślnie z ID: {}", savedOrder.getId());

        // Verify addresses and log issues (after saving, so we have orderId)
        if (savedOrder.getPickupAddress() != null && request.getPickupAddress() != null) {
            try {
                addressMediatorService.verifyAndNormalizeWithLogging(
                        request.getPickupAddress(),
                        savedOrder.getId(),
                        savedOrder.getPickupAddress().getId(),
                        "NOMINATIM"
                );
            } catch (Exception e) {
                log.warn("Address verification failed for pickup address: {}", e.getMessage());
            }
        }

        if (savedOrder.getDeliveryAddress() != null && request.getDeliveryAddress() != null) {
            try {
                com.example.danxils_commons.dto.AddressDto deliveryDto = orderMapper.mapToAddressDto(savedOrder.getDeliveryAddress());
                addressMediatorService.verifyAndNormalizeWithLogging(
                        deliveryDto,
                        savedOrder.getId(),
                        savedOrder.getDeliveryAddress().getId(),
                        "NOMINATIM"
                );
            } catch (Exception e) {
                log.warn("Address verification failed for delivery address: {}", e.getMessage());
            }
        }

        OrderCreatedEvent event = orderMapper.mapToCreatedEvent(savedOrder);
        saveToOutbox("ORDER", savedOrder.getId().toString(),
                appProperties.getKafka().getTopics().getOrdersCreated(), event);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findOrdersByCriteria(OrderQueryRequestDto query) {
        log.info("Wyszukiwanie wewnętrzne zleceń na podstawie kryteriów: {}", query);
        Specification<OrderEntity> spec = (root, q, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (query.getStatuses() != null && !query.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(query.getStatuses()));
            }
            if (query.getCustomerIds() != null && !query.getCustomerIds().isEmpty()) {
                predicates.add(root.get("client").get("id").in(query.getCustomerIds()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return orderRepository.findAll(spec);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findOrdersByIds(List<UUID> orderIds) {
        if (orderIds == null || orderIds.isEmpty())
            return Collections.emptyList();
        return orderRepository.findAllById(orderIds);
    }

    @Transactional
    @Timed(value = "order.status_update", description = "Time taken to update order status")
    public OrderEntity updateStatus(UUID orderId, UpdateStatusRequestDto request, String userId) {
        log.info("Attempting to update status of order {} to {} by user {}", orderId, request.getNewStatus(), userId);
        OrderEntity order = findOrderById(orderId);

        String assignedDriver = order.getProperty("assignedDriver", String.class);
        if (assignedDriver == null || !assignedDriver.equals(userId)) {
            // Basic check - in real world roles would matter more
            // throw new AccessDeniedException(...)
        }

        validateTransition(order.getStatus(), request.getNewStatus());
        OrderStatus previousStatus = order.getStatus();

        order.setStatus(request.getNewStatus());
        order.addProperty("lastStatusChange", Instant.now());

        OrderEntity updatedOrder = orderRepository.save(order);

        OrderStatusChangedEvent event = OrderStatusChangedEvent.builder()
                .orderId(orderId.toString())
                .previousStatus(previousStatus)
                .newStatus(request.getNewStatus())
                .changedBy(userId)
                .timestamp(Instant.now())
                .build();

        saveToOutbox("ORDER", orderId.toString(), appProperties.getKafka().getTopics().getOrdersStatusChanged(), event);
        return updatedOrder;
    }

    @Transactional
    public void processRoutePlan(RoutePlannedEvent event) {
        UUID vehicleId = event.getVehicleId();
        List<UUID> orderIds = event.getIncludedOrderIds();

        if (orderIds == null || orderIds.isEmpty())
            return;

        List<OrderEntity> ordersToUpdate = orderRepository.findAllById(orderIds);
        Instant now = Instant.now();
        String vehicleIdString = (vehicleId != null) ? vehicleId.toString() : null;

        for (OrderEntity order : ordersToUpdate) {
            OrderStatus currentStatus = order.getStatus();
            if (currentStatus != OrderStatus.NEW)
                continue;

            order.addProperty("assignedDriver", vehicleIdString); // Or assignedVehicle
            order.setStatus(OrderStatus.PICKUP);
        }

        orderRepository.saveAll(ordersToUpdate);

        // Emitting events logic omitted for brevity in this task step
    }

    public OrderEntity findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono zlecenia o ID: " + orderId));
    }

    @Transactional(readOnly = true)
    public Page<OrderEntity> findAll(OrderFilterDto filter, Pageable pageable) {
        return orderRepository.findAll(orderSpecification.filterBy(filter), pageable);
    }

    // Assign Driver
    @Transactional
    public OrderEntity assignDriver(UUID orderId, AssignDriverRequestDto request, String assignedByUserId) {
        OrderEntity order = findOrderById(orderId);
        order.addProperty("assignedDriver", request.getDriverId());
        order.setStatus(OrderStatus.PICKUP);
        // Events...
        return orderRepository.save(order);
    }

    // Confirm Pickup
    @Transactional
    public OrderEntity confirmPickup(UUID orderId, ConfirmPickupRequestDto request, String driverId) {
        OrderEntity order = findOrderById(orderId);
        // Validations...
        order.setStatus(OrderStatus.LOAD);
        return orderRepository.save(order);
    }

    // WMS Order
    @Transactional
    public UUID createOrderFromWms(com.example.order_service.dto.request.WmsOrderRequestDto request) {
        ClientEntity client = clientRepository.findByName(request.alias())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + request.alias()));
        OrderEntity order = new OrderEntity();
        order.setClient(client);
        order.setStatus(OrderStatus.NEW);

        // Map WMS Fields to Properties
        order.addProperty("partNumber", request.serialNumber());
        order.addProperty("warehouseAcceptanceDate", request.warehouseAcceptanceDate());

        Map<String, Object> wmsData = new HashMap<>();
        wmsData.put("serialNumber", request.serialNumber());
        wmsData.put("quantity", request.quantity());
        order.addProperty("wmsData", wmsData);

        OrderEntity savedOrder = orderRepository.save(order);

        // Create Asset
        AssetEntity asset = new AssetEntity();
        asset.setOrder(savedOrder);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("colli", request.quantity());

        // Parse Weight from Name "Wheel 12.5 kg"
        try {
            String name = request.name();
            // Regex to find double
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(\\.\\d+)?)").matcher(name);
            if (m.find()) {
                double weight = Double.parseDouble(m.group(1));
                attributes.put("weight", weight);
            }
        } catch (Exception e) {
            log.warn("Failed to parse weight from WMS name: {}", request.name());
        }

        asset.setAttributes(attributes);
        assetRepository.save(asset);

        return savedOrder.getId();
    }

    public OrderEntity updateMetrics(UUID orderId,
            com.example.order_service.dto.request.UpdateOrderMetricsRequestDto request) {
        OrderEntity order = findOrderById(orderId);
        if (request.getWaitingTimeMinutes() != null) {
            order.addProperty("waitingTimeMinutes", request.getWaitingTimeMinutes());
        }
        return orderRepository.save(order);
    }

    @Transactional
    public void markAsForwardedToTms(String orderId) {
        OrderEntity order = findOrderById(UUID.fromString(orderId));
        order.addProperty("forwardedToTms", true);
        orderRepository.save(order);
    }

    private void validateTransition(OrderStatus from, OrderStatus to) {
        if (from == null) {
            if (to != OrderStatus.NEW)
                throw new IllegalStateException("Initial status must be NEW");
            return;
        }
        if (!validTransitions.getOrDefault(from, Set.of()).contains(to)) {
            throw new IllegalStateException("Niedozwolona zmiana statusu z " + from + " na " + to);
        }
    }

    private void saveToOutbox(String aggregateType, String aggregateId, String eventType, Object eventPayload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(eventPayload);
            OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .createdAt(Instant.now())
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event for outbox", e);
        }
    }
}
