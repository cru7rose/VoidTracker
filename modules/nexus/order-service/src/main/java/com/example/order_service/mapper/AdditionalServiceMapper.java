package com.example.order_service.mapper;

import com.example.danxils_commons.dto.AdditionalServiceDto;
import com.example.order_service.entity.AdditionalServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * ARCHITEKTURA: Komponent mapujący dla logiki zarządzania słownikiem usług.
 * Wykorzystuje MapStruct do transformacji między kontraktem DTO
 * a encją bazodanową.
 */
@org.springframework.stereotype.Component
public class AdditionalServiceMapper {

    public AdditionalServiceEntity toEntity(AdditionalServiceDto dto) {
        if (dto == null) {
            return null;
        }

        AdditionalServiceEntity additionalServiceEntity = new AdditionalServiceEntity();

        additionalServiceEntity.setId(dto.id());
        additionalServiceEntity.setServiceCode(dto.serviceCode());
        additionalServiceEntity.setName(dto.name());
        additionalServiceEntity.setDescription(dto.description());
        if (dto.inputType() != null) {
            additionalServiceEntity
                    .setInputType(com.example.order_service.entity.ServiceInputType.valueOf(dto.inputType()));
        }

        return additionalServiceEntity;
    }

    public AdditionalServiceDto toDto(AdditionalServiceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AdditionalServiceDto(
                entity.getId(),
                entity.getServiceCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getInputType() != null ? entity.getInputType().name() : null);
    }
}