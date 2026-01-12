package com.example.planning_service.mapper;

import com.example.danxils_commons.dto.FleetVehicleDto;
import com.example.planning_service.entity.FleetVehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * ARCHITEKTURA: Komponent mapujący dla logiki zarządzania flotą.
 * Wykorzystuje MapStruct do automatyzacji transformacji między kontraktem
 * DTO a encją bazodanową.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FleetVehicleMapper {
    FleetVehicleEntity toEntity(FleetVehicleDto dto);
    FleetVehicleDto toDto(FleetVehicleEntity entity);
}