package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "webhook_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookConfigEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String eventType; // e.g., "DRIVER_ASSIGNED", "GEOFENCE_VIOLATION"

    @Column(nullable = false)
    private String url; // The n8n webhook URL

    private String authHeader; // API Key or Auth Header value

    private boolean active;

    @Column(columnDefinition = "TEXT")
    private String description;
}
