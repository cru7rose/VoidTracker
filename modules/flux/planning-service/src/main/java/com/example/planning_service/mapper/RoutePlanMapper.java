// File: planning-service/src/main/java/com/example/planning_service/mapper/RoutePlanMapper.java
package com.example.planning_service.mapper;

import com.example.planning_service.dto.RoutePlanDto;
import com.example.planning_service.entity.RoutePlanEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * ARCHITEKTURA: Komponent mapujący dla logiki szablonów planów tras (RoutePlan).
 * Wykorzystuje MapStruct do automatyzacji transformacji między DTO (kontrakt API)
 * a encją (model bazodanowy), co upraszcza kod serwisów i kontrolerów.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RoutePlanMapper {

    RoutePlanEntity toEntity(RoutePlanDto dto);

    RoutePlanDto toDto(RoutePlanEntity entity);
}