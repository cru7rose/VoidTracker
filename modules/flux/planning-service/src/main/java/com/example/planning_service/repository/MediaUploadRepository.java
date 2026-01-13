package com.example.planning_service.repository;

import com.example.planning_service.entity.MediaUploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for MediaUpload persistence operations
 */
@Repository
public interface MediaUploadRepository extends JpaRepository<MediaUploadEntity, UUID> {

    /**
     * Find all media uploads for a specific route
     */
    List<MediaUploadEntity> findByRouteId(UUID routeId);

    /**
     * Find all media uploads for a specific stop
     */
    List<MediaUploadEntity> findByStopId(UUID stopId);

    /**
     * Find all media uploads for a specific route and media type
     */
    List<MediaUploadEntity> findByRouteIdAndMediaType(UUID routeId, MediaUploadEntity.MediaType mediaType);

    /**
     * Find all media uploads for a specific stop and media type
     */
    List<MediaUploadEntity> findByStopIdAndMediaType(UUID stopId, MediaUploadEntity.MediaType mediaType);

    /**
     * Find all media uploads for a specific driver
     */
    List<MediaUploadEntity> findByDriverId(UUID driverId);
}
