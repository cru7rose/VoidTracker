package com.example.order_service.model.event;

import com.example.danxils_commons.dto.AddressDto;
import com.example.danxils_commons.dto.PackageDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCreatedEvent {
    private String orderId; // ID zlecenia z bazy voidtracker
    private String customerId;
    private String priority;
    private AddressDto pickupAddress;
    private AddressDto deliveryAddress;
    private PackageDto packageDetails;
}