package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity for storing media upload metadata (photos, signatures).
 * Files are stored on filesystem or S3, this entity stores only metadata.
 */
@Entity
@Table(name = "media_uploads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Route assignment ID this media belongs to
     */
    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    /**
     * Stop ID within the route (optional, for stop-specific media)
     */
    @Column(name = "stop_id")
    private UUID stopId;

    /**
     * Order ID (optional, for order-specific media)
     */
    @Column(name = "order_id")
    private UUID orderId;

    /**
     * Media type: DMG (damage), POD (proof of delivery), SIGNATURE
     */
    @Column(name = "media_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    /**
     * File path (relative to storage root, e.g., /uploads/{routeId}/{stopId}/pod_001.jpg)
     * Or S3 key if using S3 storage
     */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    /**
     * Original filename
     */
    @Column(name = "original_filename")
    private String originalFilename;

    /**
     * File size in bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * MIME type (e.g., image/jpeg, image/png)
     */
    @Column(name = "mime_type")
    private String mimeType;

    /**
     * GPS coordinates from EXIF (if available)
     */
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    /**
     * Driver ID who uploaded this media
     */
    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    /**
     * Upload timestamp
     */
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    /**
     * Audit: Who uploaded (user ID or system)
     */
    @Column(name = "uploaded_by")
    private String uploadedBy;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
    }

    public enum MediaType {
        DMG,        // Damage photo
        POD,        // Proof of delivery photo
        SIGNATURE   // Signature image
    }
}
