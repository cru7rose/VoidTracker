package com.example.planning_service.service;

import com.example.planning_service.dto.MediaUploadResponseDto;
import com.example.planning_service.entity.MediaUploadEntity;
import com.example.planning_service.entity.RouteAssignmentEntity;
import com.example.planning_service.repository.MediaUploadRepository;
import com.example.planning_service.repository.RouteAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

/**
 * Service for handling media uploads (photos, signatures) from Ghost PWA.
 * Supports DMG (damage), POD (proof of delivery), and signature uploads.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaUploadService {

    private final MediaUploadRepository mediaUploadRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;

    @Value("${media.upload.directory:./uploads}")
    private String uploadDirectory;

    @Value("${media.upload.max-size:10485760}") // 10MB default
    private long maxFileSize;

    /**
     * Upload media file (photo or signature)
     * 
     * @param routeId Route assignment ID
     * @param stopId Optional stop ID
     * @param orderId Optional order ID
     * @param mediaType DMG, POD, or SIGNATURE
     * @param driverId Driver who is uploading
     * @param file Multipart file
     * @return MediaUploadResponseDto with upload metadata
     */
    @Transactional
    public MediaUploadResponseDto uploadMedia(
            UUID routeId,
            UUID stopId,
            UUID orderId,
            MediaUploadEntity.MediaType mediaType,
            UUID driverId,
            MultipartFile file) throws IOException {

        // Validate file
        validateFile(file);

        // Verify route exists and driver has access
        RouteAssignmentEntity route = routeAssignmentRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));

        if (!driverId.equals(route.getDriverId())) {
            throw new IllegalStateException("Driver does not have access to this route");
        }

        // Generate unique filename
        String filename = generateFilename(routeId, stopId, mediaType, file.getOriginalFilename());
        
        // Create directory structure: uploads/{routeId}/{stopId}/
        Path uploadPath = createUploadPath(routeId, stopId);
        Path filePath = uploadPath.resolve(filename);

        // Save file to filesystem
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Saved media file: {}", filePath);

        // Extract EXIF geolocation (if available)
        // TODO: Implement EXIF extraction using metadata-extractor library
        Double latitude = null;
        Double longitude = null;
        // For now, we'll skip EXIF extraction - can be added later

        // Save metadata to database
        MediaUploadEntity entity = MediaUploadEntity.builder()
                .routeId(routeId)
                .stopId(stopId)
                .orderId(orderId)
                .mediaType(mediaType)
                .filePath(filePath.toString().replace("\\", "/")) // Normalize path separators
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .latitude(latitude)
                .longitude(longitude)
                .driverId(driverId)
                .uploadedAt(Instant.now())
                .uploadedBy(driverId.toString())
                .build();

        MediaUploadEntity saved = mediaUploadRepository.save(entity);

        // Convert to DTO
        return toDto(saved);
    }

    /**
     * Get media by ID
     */
    public MediaUploadResponseDto getMedia(UUID mediaId) {
        MediaUploadEntity entity = mediaUploadRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));
        return toDto(entity);
    }

    /**
     * Get all media for a route
     */
    public java.util.List<MediaUploadResponseDto> getMediaByRoute(UUID routeId) {
        return mediaUploadRepository.findByRouteId(routeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Get all media for a stop
     */
    public java.util.List<MediaUploadResponseDto> getMediaByStop(UUID stopId) {
        return mediaUploadRepository.findByStopId(stopId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Delete media (soft delete - mark as deleted, or hard delete file)
     */
    @Transactional
    public void deleteMedia(UUID mediaId, UUID driverId) {
        MediaUploadEntity entity = mediaUploadRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));

        // Verify driver owns this media
        if (!driverId.equals(entity.getDriverId())) {
            throw new IllegalStateException("Driver does not have permission to delete this media");
        }

        // Delete file from filesystem
        try {
            Path filePath = Paths.get(entity.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted media file: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete media file: {}", entity.getFilePath(), e);
        }

        // Delete from database
        mediaUploadRepository.delete(entity);
    }

    // ========== Helper Methods ==========

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum: " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
    }

    private String generateFilename(UUID routeId, UUID stopId, MediaUploadEntity.MediaType mediaType, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            extension = ".jpg"; // Default
        }

        String prefix = mediaType.name().toLowerCase();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        if (stopId != null) {
            return String.format("%s_%s_%s%s", prefix, stopId.toString().substring(0, 8), uniqueId, extension);
        } else {
            return String.format("%s_%s_%s%s", prefix, timestamp, uniqueId, extension);
        }
    }

    private Path createUploadPath(UUID routeId, UUID stopId) throws IOException {
        Path basePath = Paths.get(uploadDirectory);
        Path routePath = basePath.resolve(routeId.toString());
        Path stopPath = stopId != null ? routePath.resolve(stopId.toString()) : routePath;

        Files.createDirectories(stopPath);
        return stopPath;
    }

    private MediaUploadResponseDto toDto(MediaUploadEntity entity) {
        return MediaUploadResponseDto.builder()
                .id(entity.getId())
                .routeId(entity.getRouteId())
                .stopId(entity.getStopId())
                .orderId(entity.getOrderId())
                .mediaType(entity.getMediaType().name())
                .filePath(entity.getFilePath())
                .originalFilename(entity.getOriginalFilename())
                .fileSize(entity.getFileSize())
                .mimeType(entity.getMimeType())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .driverId(entity.getDriverId())
                .uploadedAt(entity.getUploadedAt())
                .downloadUrl("/api/planning/media/" + entity.getId() + "/download")
                .build();
    }
}
