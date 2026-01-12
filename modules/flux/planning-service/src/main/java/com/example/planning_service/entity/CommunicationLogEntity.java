package com.example.planning_service.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "communication_logs")
@Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class CommunicationLogEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private java.time.Instant timestamp; // Changed to Instant for consistency if possible, or keep LocalDateTime. Let's
                                         // try Instant as Controller used it.
    // Wait, previous file had LocalDateTime. Let's change to Instant to match
    // modern standards in this project.

    @Column(nullable = true)
    private String recipient; // Renamed from contact to match intent better, or alias? Let's use 'recipient'
                              // to match my Controller code.

    @Column(nullable = true)
    private String channel; // Renamed from type

    // @Column(nullable = false)
    // private String direction; // Removing strictly required direction if not
    // used, or defaulting it.

    @Column(columnDefinition = "TEXT")
    private String messageContent; // Renamed from content

    @Column(nullable = false)
    private String status;

    @org.hibernate.annotations.Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(columnDefinition = "jsonb")
    private java.util.Map<String, Object> metadata;
}
