package com.example.driver_app_service.mapper;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.driver_app_service.dto.DriverTaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * ARCHITEKTURA: Komponent mapujący, który transformuje pełny, kanoniczny
 * `OrderResponseDto` na uproszczony `DriverTaskDto` dla widoku listy.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {
    @Mapping(source = "pickup.city", target = "pickupCity")
    @Mapping(source = "pickup.street", target = "pickupStreet")
    @Mapping(source = "delivery.city", target = "deliveryCity")
    @Mapping(source = "delivery.street", target = "deliveryStreet")
    @Mapping(source = "delivery.sla", target = "sla")
    DriverTaskDto toDto(OrderResponseDto orderResponseDto);

    List<DriverTaskDto> toDtoList(List<OrderResponseDto> orderResponseDtos);
}