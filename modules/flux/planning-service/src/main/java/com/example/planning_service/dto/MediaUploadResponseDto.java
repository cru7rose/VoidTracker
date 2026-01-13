package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for media upload response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadResponseDto {
    
    private UUID id;
    private UUID routeId;
    private UUID stopId;
    private UUID orderId;
    private String mediaType;
    private String filePath;
    private String originalFilename;
    private Long fileSize;
    private String mimeType;
    private Double latitude;
    private Double longitude;
    private UUID driverId;
    private Instant uploadedAt;
    
    /**
     * URL to access the media (for frontend display)
     * Format: /api/planning/media/{id}/download
     */
    private String downloadUrl;
}
