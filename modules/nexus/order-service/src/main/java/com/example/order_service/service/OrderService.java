package com.example.order_service.service;

import com.example.danxils_commons.dto.OrderQueryRequestDto;
import com.example.danxils_commons.enums.OrderStatus;
import com.example.danxils_commons.event.RoutePlannedEvent;
import com.example.order_service.dto.filter.OrderFilterDto;
import com.example.order_service.dto.request.AssignDriverRequestDto;
import com.example.order_service.dto.request.ConfirmPickupRequestDto;
import com.example.order_service.dto.request.CreateOrderRequestDto;
import com.example.order_service.dto.request.UpdateOrderMetricsRequestDto;
import com.example.order_service.dto.request.UpdateStatusRequestDto;
import com.example.order_service.dto.request.WmsOrderRequestDto;
import com.example.order_service.entity.AddressEntity;
import com.example.order_service.entity.ClientEntity;
import com.example.order_service.entity.OrderEntity;
import com.example.order_service.entity.OutboxEventEntity;
import com.example.order_service.entity.PackageEntity;
import com.example.order_service.repository.ClientRepository;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.OutboxEventRepository;
import com.example.order_service.repository.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final OrderSpecification orderSpecification;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public OrderEntity createOrder(CreateOrderRequestDto request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        // Find or create client
        ClientEntity client = clientRepository.findByExternalId(request.getCustomerId())
                .orElseGet(() -> {
                    ClientEntity newClient = new ClientEntity();
                    newClient.setExternalId(request.getCustomerId());
                    newClient.setName(request.getCustomerId()); // Fallback name
                    return clientRepository.save(newClient);
                });

        // Create order entity
        OrderEntity order = new OrderEntity();
        order.setExternalId(request.getExternalReference());
        order.setClient(client);
        order.setStatus(OrderStatus.NEW);
        order.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000")); // Default tenant

        // Map addresses
        if (request.getPickupAddress() != null) {
            AddressEntity pickupAddress = mapToAddressEntity(request.getPickupAddress());
            pickupAddress.setOwnerClient(client);
            order.setPickupAddress(pickupAddress);
        }

        if (request.getDeliveryAddress() != null) {
            AddressEntity deliveryAddress = mapToDeliveryAddressEntity(request.getDeliveryAddress());
            deliveryAddress.setOwnerClient(client);
            order.setDeliveryAddress(deliveryAddress);
        }

        // Map package
        if (request.getPackageDetails() != null) {
            PackageEntity packageEntity = mapToPackageEntity(request.getPackageDetails());
            order.setPackageDetails(packageEntity);
        }

        // Set time windows
        order.setPickupTimeFrom(request.getPickupTimeFrom());
        order.setPickupTimeTo(request.getPickupTimeTo());
        order.setDeliveryTimeFrom(request.getDeliveryTimeFrom());
        order.setDeliveryTimeTo(request.getDeliveryTimeTo());

        // Store additional properties
        if (request.getPriority() != null) {
            order.addProperty("priority", request.getPriority());
        }
        if (request.getRemark() != null) {
            order.addProperty("remark", request.getRemark());
        }
        if (request.getRequiredServiceCodes() != null) {
            order.addProperty("requiredServiceCodes", request.getRequiredServiceCodes());
        }
        if (request.getInjectionHubId() != null) {
            order.setInjectionHub(request.getInjectionHubId());
        }
        if (request.getCostCenter() != null) {
            order.addProperty("costCenter", request.getCostCenter());
        }
        if (request.getIsTransfer() != null) {
            order.addProperty("isTransfer", request.getIsTransfer());
        }

        OrderEntity saved = orderRepository.save(order);
        log.info("Created order with ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public UUID createOrderFromWms(WmsOrderRequestDto wmsRequest) {
        log.info("Creating order from WMS: {}", wmsRequest.alias());

        // Find or create client
        ClientEntity client = clientRepository.findByExternalId(wmsRequest.alias())
                .orElseGet(() -> {
                    ClientEntity newClient = new ClientEntity();
                    newClient.setExternalId(wmsRequest.alias());
                    newClient.setName(wmsRequest.name());
                    return clientRepository.save(newClient);
                });

        // Create order entity
        OrderEntity order = new OrderEntity();
        order.setExternalId(wmsRequest.serialNumber());
        order.setClient(client);
        order.setStatus(OrderStatus.NEW);
        order.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        // Store WMS-specific data in properties
        order.addProperty("wmsSerialNumber", wmsRequest.serialNumber());
        order.addProperty("wmsName", wmsRequest.name());
        order.addProperty("wmsQuantity", wmsRequest.quantity());
        order.addProperty("wmsWarehouseAcceptanceDate", wmsRequest.warehouseAcceptanceDate());

        OrderEntity saved = orderRepository.save(order);
        log.info("Created WMS order with ID: {}", saved.getId());
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public Page<OrderEntity> findAll(OrderFilterDto filter, Pageable pageable) {
        Specification<OrderEntity> spec = orderSpecification.filterBy(filter);
        return orderRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public OrderEntity findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findOrdersByCriteria(OrderQueryRequestDto query) {
        if (query == null) {
            return List.of();
        }

        // Build specification from query criteria
        Specification<OrderEntity> spec = Specification.where(null);

        if (query.getStatuses() != null && !query.getStatuses().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    root.get("status").in(query.getStatuses()));
        }

        if (query.getCustomerIds() != null && !query.getCustomerIds().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    root.get("client").get("id").in(query.getCustomerIds()));
        }

        if (query.getPostalCodePrefixes() != null && !query.getPostalCodePrefixes().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) -> {
                var predicates = query.getPostalCodePrefixes().stream()
                        .map(prefix -> criteriaBuilder.like(
                                root.get("deliveryAddress").get("postalCode"), prefix + "%"))
                        .toList();
                return criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            });
        }

        if (query.getPriorities() != null && !query.getPriorities().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) -> {
                var predicates = query.getPriorities().stream()
                        .map(priority -> criteriaBuilder.equal(
                                criteriaBuilder.function("jsonb_extract_path_text",
                                        String.class,
                                        root.get("properties"),
                                        criteriaBuilder.literal("priority")),
                                priority))
                        .toList();
                return criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            });
        }

        return orderRepository.findAll(spec);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findOrdersByIds(List<UUID> orderIds) {
        return orderRepository.findAllById(orderIds);
    }

    @Transactional
    public OrderEntity assignDriver(UUID orderId, AssignDriverRequestDto request, String username) {
        log.info("Assigning driver {} to order {} by user {}", request.getDriverId(), orderId, username);

        OrderEntity order = findOrderById(orderId);

        // Validate state transition
        if (!order.getStatus().canTransitionTo(OrderStatus.PICKUP)) {
            throw new IllegalStateException(
                    "Cannot assign driver: order status " + order.getStatus() + " does not allow transition to PICKUP");
        }

        order.setAssignedDriver(request.getDriverId());
        order.setStatus(OrderStatus.PICKUP);
        order.addProperty("assignedBy", username);
        order.addProperty("assignedAt", Instant.now());

        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity confirmPickup(UUID orderId, ConfirmPickupRequestDto request, String username) {
        log.info("Confirming pickup for order {} by user {}", orderId, username);

        OrderEntity order = findOrderById(orderId);

        // Validate state transition
        if (!order.getStatus().canTransitionTo(OrderStatus.LOAD)) {
            throw new IllegalStateException(
                    "Cannot confirm pickup: order status " + order.getStatus() + " does not allow transition to LOAD");
        }

        // Store scanned barcodes
        order.addProperty("scannedBarcodes", request.getScannedBarcodes());
        order.addProperty("pickupConfirmedBy", username);
        order.addProperty("pickupConfirmedAt", Instant.now());

        order.setStatus(OrderStatus.LOAD);

        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity updateMetrics(UUID orderId, UpdateOrderMetricsRequestDto request) {
        log.info("Updating metrics for order {}", orderId);

        OrderEntity order = findOrderById(orderId);
        order.addProperty("waitingTimeMinutes", request.getWaitingTimeMinutes());
        order.addProperty("metricsUpdatedAt", Instant.now());

        return orderRepository.save(order);
    }

    @Transactional
    public void processRoutePlan(RoutePlannedEvent event) {
        log.info("Processing route plan {} with {} orders", event.getPlanId(), event.getIncludedOrderIds().size());

        List<OrderEntity> orders = orderRepository.findAllById(event.getIncludedOrderIds());

        for (OrderEntity order : orders) {
            // Update order with route plan information
            order.addProperty("routePlanId", event.getPlanId());
            order.addProperty("routePlanCreatedAt", Instant.now());

            // If order is in NEW status, transition to PICKUP if driver is assigned
            if (order.getStatus() == OrderStatus.NEW && order.getAssignedDriver() != null) {
                if (order.getStatus().canTransitionTo(OrderStatus.PICKUP)) {
                    order.setStatus(OrderStatus.PICKUP);
                }
            }
        }

        orderRepository.saveAll(orders);
        log.info("Processed route plan {} for {} orders", event.getPlanId(), orders.size());
    }

    @Transactional
    public void markAsForwardedToTms(UUID orderId) {
        log.info("Marking order {} as forwarded to TMS", orderId);

        OrderEntity order = findOrderById(orderId);
        order.addProperty("forwardedToTms", true);
        order.addProperty("forwardedToTmsAt", Instant.now());

        orderRepository.save(order);
    }

    @Transactional
    public OrderEntity updateStatus(UUID orderId, UpdateStatusRequestDto request, String userId) {
        log.info("Updating status for order {} to {} by user {}", orderId, request.getNewStatus(), userId);

        OrderEntity order = findOrderById(orderId);

        // Validate state transition
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getNewStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Niedozwolona zmiana statusu z %s na %s dla zlecenia %s", 
                            currentStatus, newStatus, orderId));
        }

        // Update status
        order.setStatus(newStatus);
        order.addProperty("statusUpdatedBy", userId);
        order.addProperty("statusUpdatedAt", Instant.now());

        OrderEntity saved = orderRepository.save(order);

        // Create outbox event for status change
        OutboxEventEntity outboxEvent = new OutboxEventEntity();
        outboxEvent.setId(UUID.randomUUID());
        outboxEvent.setAggregateType("Order");
        outboxEvent.setAggregateId(orderId.toString());
        outboxEvent.setEventType("orders-status-changed");
        outboxEvent.setPayload(String.format(
                "{\"orderId\":\"%s\",\"oldStatus\":\"%s\",\"newStatus\":\"%s\",\"updatedBy\":\"%s\",\"updatedAt\":\"%s\"}",
                orderId, currentStatus, newStatus, userId, Instant.now()));
        outboxEvent.setCreatedAt(Instant.now());
        outboxEventRepository.save(outboxEvent);

        log.info("Status updated for order {} from {} to {}", orderId, currentStatus, newStatus);
        return saved;
    }

    // Helper methods for mapping DTOs to entities
    private AddressEntity mapToAddressEntity(com.example.danxils_commons.dto.AddressDto dto) {
        AddressEntity entity = new AddressEntity();
        entity.setStreet(dto.getStreet());
        entity.setStreetNumber(dto.getStreetNumber());
        entity.setPostalCode(dto.getPostalCode());
        entity.setCity(dto.getCity());
        entity.setCountry(dto.getCountry());
        if (dto.getLat() != null && dto.getLon() != null) {
            entity.setLat(dto.getLat());
            entity.setLon(dto.getLon());
        }
        return entity;
    }

    private AddressEntity mapToDeliveryAddressEntity(CreateOrderRequestDto.DeliveryAddressDto dto) {
        AddressEntity entity = new AddressEntity();
        entity.setCustomerName(dto.getCustomerName());
        entity.setAttention(dto.getAttention());
        entity.setStreet(dto.getStreet());
        entity.setStreetNumber(dto.getStreetNumber());
        entity.setApartment(dto.getApartment());
        entity.setPostalCode(dto.getPostalCode());
        entity.setCity(dto.getCity());
        entity.setCountry(dto.getCountry());
        entity.setMail(dto.getMail());
        entity.setPhone(dto.getPhone());
        entity.setNote(dto.getNote());
        if (dto.getLat() != null && dto.getLon() != null) {
            entity.setLat(dto.getLat());
            entity.setLon(dto.getLon());
        }
        return entity;
    }

    private PackageEntity mapToPackageEntity(com.example.danxils_commons.dto.PackageDto dto) {
        PackageEntity entity = new PackageEntity();
        entity.setWeight(dto.getWeight());
        entity.setLength(dto.getLength());
        entity.setWidth(dto.getWidth());
        entity.setHeight(dto.getHeight());
        entity.setBarcode1(dto.getBarcode1());
        entity.setBarcode2(dto.getBarcode2());
        // PackageDto doesn't have description, but PackageEntity does - set it to null or empty
        entity.setDescription(null);
        entity.setColli(dto.getColli());
        entity.setVolume(dto.getVolume());
        entity.setAdr(dto.getAdr());
        return entity;
    }
}
