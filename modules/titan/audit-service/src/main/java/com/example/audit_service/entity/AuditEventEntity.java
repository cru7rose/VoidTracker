package com.example.audit_service.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * ARCHITEKTURA: Encja JPA reprezentująca pojedyncze zdarzenie audytowe w systemie.
 * Jest to uniwersalna struktura zdolna do przechowywania informacji o różnych
 * typach zdarzeń. Pole `details` jest mapowane na kolumnę JSONB w bazie danych,
 * co pozwala na elastyczne przechowywanie pełnego ładunku zdarzenia.
 */
@Entity
@Table(name = "audit_events")
@Data
@NoArgsConstructor
public class AuditEventEntity {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private Instant timestamp;

    private String userId;

    private String serviceName;

    @Column(nullable = false)
    private String eventType;

    private String entityType;

    private String entityId;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;
}