package com.example.order_service.mapper;

import com.example.danxils_commons.dto.AddressDto;
import com.example.order_service.model.dto.OrderResponseDto;
import com.example.danxils_commons.dto.PackageDto;
import com.example.danxils_commons.event.OrderCreatedEvent;
import com.example.order_service.dto.request.CreateOrderRequestDto;
import com.example.order_service.entity.AddressEntity;
import com.example.order_service.entity.OrderEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = EpodMapper.class)
public abstract class OrderMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    // --- Response DTO Mapping ---

    @Mapping(target = "orderNumber", source = "externalId")
    @Mapping(target = "externalReference", source = "externalId")
    @Mapping(target = "costCenter", source = ".", qualifiedByName = "getCostCenter")
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "delivery", source = ".", qualifiedByName = "mapToDeliveryPointDto")
    @Mapping(target = "timestamps.created", source = "createdAt")
    @Mapping(target = "timestamps.lastStatusChange", source = ".", qualifiedByName = "mapLastStatusChange")
    @Mapping(target = "pickup", source = ".", qualifiedByName = "mapToPickupPointDto")
    @Mapping(target = "requiredServices", source = ".", qualifiedByName = "mapRequiredServices")
    @Mapping(target = "aPackage", source = ".", qualifiedByName = "mapPackageDetails")
    @Mapping(target = "pickupTimeFrom", source = "pickupTimeFrom")
    @Mapping(target = "pickupTimeTo", source = "pickupTimeTo")
    @Mapping(target = "deliveryTimeFrom", source = "deliveryTimeFrom")
    @Mapping(target = "deliveryTimeTo", source = "deliveryTimeTo")
    public abstract OrderResponseDto mapToResponseDto(OrderEntity orderSource);

    @Named("mapLastStatusChange")
    public java.time.Instant mapLastStatusChange(OrderEntity entity) {
        java.time.Instant val = entity.getProperty("lastStatusChange", java.time.Instant.class);
        return val != null ? val
                : (entity.getUpdatedAt() != null ? entity.getUpdatedAt().toInstant(java.time.ZoneOffset.UTC) : null);
    }

    // --- Entity Mappings (New Relational Design) ---

    public abstract AddressEntity mapToAddressEntity(AddressDto dto);

    // Explicit mapping for DeliveryAddressDto (subclass)
    public abstract AddressEntity mapToDeliveryAddressEntity(CreateOrderRequestDto.DeliveryAddressDto dto);

    @Mapping(target = "packageId", ignore = true) // Generated
    public abstract com.example.order_service.entity.PackageEntity mapToPackageEntity(PackageDto dto);

    // --- Response sub-mappings (Reading from Entity Fields) ---

    @Named("mapToPickupPointDto")
    public OrderResponseDto.PickupPointDto mapToPickupPointDto(OrderEntity entity) {
        if (entity.getPickupAddress() == null)
            return null;
        AddressEntity adr = entity.getPickupAddress();

        return new OrderResponseDto.PickupPointDto(
                entity.getClient() != null ? entity.getClient().getName() : null, // customer
                null, // alias
                adr.getCountry(), // country
                null, // addressId
                adr.getPostalCode(),
                adr.getCity(),
                adr.getStreet(),
                adr.getStreetNumber(),
                adr.getCustomerName(), // name
                adr.getAttention(), // attention
                adr.getRoute(), // route
                adr.getRoutePart(), // routePart
                adr.getType(), // type
                null, // manifestDate
                null, // windowFrom
                null, // windowTo
                adr.getMail(), // mail
                adr.getPhone(), // phone
                adr.getNote(), // note
                adr.getLat(), adr.getLon());
    }

    @Named("mapToDeliveryPointDto")
    public OrderResponseDto.DeliveryPointDto mapToDeliveryPointDto(OrderEntity entity) {
        if (entity.getDeliveryAddress() == null)
            return null;
        AddressEntity adr = entity.getDeliveryAddress();

        return new OrderResponseDto.DeliveryPointDto(
                entity.getClient() != null ? entity.getClient().getName() : null, // customer
                null, // alias
                adr.getCountry(), // country
                null, // addressId
                adr.getPostalCode(),
                adr.getCity(),
                adr.getStreet(),
                adr.getStreetNumber(),
                adr.getCustomerName(), // name
                adr.getAttention(), // attention
                adr.getRoute(), // route
                adr.getRoutePart(), // routePart
                adr.getType(), // type
                null, // manifestDate
                adr.getSla(), // sla
                null, // windowFrom
                null, // windowTo
                adr.getMail(), // mail
                adr.getPhone(), // phone
                adr.getNote(), // note
                adr.getLat(), adr.getLon());
    }

    @Named("mapPackageDetails")
    public OrderResponseDto.PackageDetailsDto mapPackageDetails(OrderEntity entity) {
        if (entity.getPackageDetails() == null)
            return null;
        com.example.order_service.entity.PackageEntity pkg = entity.getPackageDetails();

        OrderResponseDto.PackageDimensionsDto dims = new OrderResponseDto.PackageDimensionsDto(
                pkg.getLength(), pkg.getWidth(), pkg.getHeight());

        Double priceDouble = pkg.getPrice() != null ? pkg.getPrice().doubleValue() : null;

        return new OrderResponseDto.PackageDetailsDto(
                pkg.getBarcode1(), pkg.getBarcode2(), pkg.getColli(), pkg.getWeight(), pkg.getVolume(),
                pkg.getRouteDistance(), pkg.getServiceType(), dims, pkg.getDriverNote(), pkg.getInvoiceNote(),
                priceDouble, pkg.getCurrency(), pkg.getAdr());
    }

    @Named("mapRequiredServices")
    public List<String> mapRequiredServices(OrderEntity entity) {
        if (entity.getProperties() == null)
            return null;
        return objectMapper.convertValue(entity.getProperties().get("requiredServiceCodes"), List.class);
    }

    // --- Helper Getters from Properties ---

    @Named("getCostCenter")
    public String getCostCenter(OrderEntity entity) {
        return entity.getProperty("costCenter", String.class);
    }

    @Named("getPriority")
    public String getPriority(OrderEntity entity) {
        return entity.getProperty("priority", String.class);
    }

    public java.time.Instant map(java.time.LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toInstant(java.time.ZoneOffset.UTC);
    }

    // --- Order Created Event Mapping ---

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "customerId", expression = "java(orderSource.getClient() != null ? orderSource.getClient().getId().toString() : null)")
    @Mapping(target = "priority", source = ".", qualifiedByName = "getPriority")
    @Mapping(target = "pickupAddress", source = "pickupAddress")
    @Mapping(target = "deliveryAddress", source = "deliveryAddress")
    @Mapping(target = "packageDetails", source = "packageDetails")
    public abstract OrderCreatedEvent mapToCreatedEvent(OrderEntity orderSource);

    // Helpers for Event mapping DTOs
    public abstract AddressDto mapToAddressDto(AddressEntity entity);

    public abstract com.example.danxils_commons.event.OrderCreatedEvent.DeliveryAddressDto mapToEventDeliveryAddressDto(
            AddressEntity entity);

    public abstract PackageDto mapToPackageDto(com.example.order_service.entity.PackageEntity entity);
}