package com.example.order_service.entity;

import com.example.danxils_commons.enums.ScanType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Operational "Source of Truth" for where an asset is.
 * Mutable operational record, unlike the immutable Chain of Custody blockchain.
 */
@Entity
@Table(name = "vt_scan_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "hub_id")
    private UUID hubId; // Nullable if scan happened on the road (Driver)

    @Column(name = "actor_id", nullable = false)
    private UUID actorId; // User who scanned

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_type", nullable = false)
    private ScanType scanType;

    @Column(nullable = false)
    private Instant timestamp;

    /**
     * GPS Coordinates of the scan.
     * { "lat": 52.5200, "lon": 13.4050, "accuracy": 10.5 }
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> gps;

    /**
     * Extra metadata (e.g., photo URL, damage report).
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

}
