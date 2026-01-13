package com.example.planning_service.controller;

import com.example.planning_service.dto.MediaUploadResponseDto;
import com.example.planning_service.entity.MediaUploadEntity;
import com.example.planning_service.service.MediaUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * REST API for media uploads from Ghost PWA.
 * Handles photo uploads (DMG, POD) and signature uploads.
 */
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
public class MediaUploadController {

    private final MediaUploadService mediaUploadService;

    /**
     * Upload media file
     * POST /api/planning/media/upload/{type}
     * 
     * @param type Media type: dmg, pod, or signature
     * @param routeId Route assignment ID (required)
     * @param stopId Stop ID (optional)
     * @param orderId Order ID (optional)
     * @param driverId Driver ID (required, for authorization)
     * @param file Multipart file (image)
     */
    @PostMapping("/upload/{type}")
    public ResponseEntity<MediaUploadResponseDto> uploadMedia(
            @PathVariable String type,
            @RequestParam UUID routeId,
            @RequestParam(required = false) UUID stopId,
            @RequestParam(required = false) UUID orderId,
            @RequestParam UUID driverId,
            @RequestParam("file") MultipartFile file) {

        try {
            MediaUploadEntity.MediaType mediaType = parseMediaType(type);
            
            MediaUploadResponseDto response = mediaUploadService.uploadMedia(
                    routeId, stopId, orderId, mediaType, driverId, file);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid upload request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            log.warn("Unauthorized upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get media by ID
     * GET /api/planning/media/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MediaUploadResponseDto> getMedia(@PathVariable UUID id) {
        try {
            MediaUploadResponseDto media = mediaUploadService.getMedia(id);
            return ResponseEntity.ok(media);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get all media for a route
     * GET /api/planning/media/route/{routeId}
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<MediaUploadResponseDto>> getMediaByRoute(@PathVariable UUID routeId) {
        List<MediaUploadResponseDto> media = mediaUploadService.getMediaByRoute(routeId);
        return ResponseEntity.ok(media);
    }

    /**
     * Get all media for a stop
     * GET /api/planning/media/stop/{stopId}
     */
    @GetMapping("/stop/{stopId}")
    public ResponseEntity<List<MediaUploadResponseDto>> getMediaByStop(@PathVariable UUID stopId) {
        List<MediaUploadResponseDto> media = mediaUploadService.getMediaByStop(stopId);
        return ResponseEntity.ok(media);
    }

    /**
     * Download media file
     * GET /api/planning/media/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMedia(@PathVariable UUID id) {
        try {
            MediaUploadResponseDto media = mediaUploadService.getMedia(id);
            Path filePath = Paths.get(media.getFilePath());

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Resource resource = new FileSystemResource(filePath.toFile());
            String contentType = media.getMimeType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + media.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Download failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete media
     * DELETE /api/planning/media/{id}?driverId={driverId}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable UUID id,
            @RequestParam UUID driverId) {
        try {
            mediaUploadService.deleteMedia(id, driverId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private MediaUploadEntity.MediaType parseMediaType(String type) {
        return switch (type.toLowerCase()) {
            case "dmg" -> MediaUploadEntity.MediaType.DMG;
            case "pod" -> MediaUploadEntity.MediaType.POD;
            case "signature" -> MediaUploadEntity.MediaType.SIGNATURE;
            default -> throw new IllegalArgumentException("Invalid media type: " + type);
        };
    }
}
