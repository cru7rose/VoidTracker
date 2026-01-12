// File: order-service/src/main/java/com/example/order_service/mapper/EpodMapper.java
package com.example.order_service.mapper;

import com.example.danxils_commons.dto.ePoDDto;
import com.example.order_service.entity.ePoDEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ARCHITEKTURA: Komponent mapujący dla logiki ePoD (Electronic Proof of
 * Delivery).
 * Changed to abstract class to resolve "Duplicate method" generation issue.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class EpodMapper {

    @Mapping(target = "orderId", ignore = true) // Ustawiane ręcznie w serwisie
    @Mapping(target = "userId", ignore = true) // Ustawiane ręcznie w serwisie
    @Mapping(source = "location.lat", target = "lat")
    @Mapping(source = "location.lng", target = "lng")
    public abstract ePoDEntity mapToEntity(ePoDDto dto);

    @Mapping(source = "lat", target = "location.lat")
    @Mapping(source = "lng", target = "location.lng")
    public abstract ePoDDto mapToDto(ePoDEntity entity);

    /**
     * Converts a List of photo URLs into a single comma-separated string for DB
     * persistence.
     */
    public String mapPhotosToString(List<String> photos) {
        if (photos == null || photos.isEmpty()) {
            return null;
        }
        return String.join(",", photos);
    }

    /**
     * Converts a comma-separated string from the DB back into a List of photo URLs.
     */
    public List<String> mapPhotosToList(String photos) {
        if (photos == null || photos.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(photos.split(","));
    }
}