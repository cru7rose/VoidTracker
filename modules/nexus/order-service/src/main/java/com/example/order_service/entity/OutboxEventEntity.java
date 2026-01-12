package com.example.order_service.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Encja JPA reprezentująca pojedyncze zdarzenie w tabeli outbox.
 * Przechowuje zserializowany ładunek zdarzenia (payload) w formacie JSONB
 * oraz metadane niezbędne do jego późniejszej publikacji.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(nullable = false)
    private Instant createdAt;

    public OutboxEventEntity() {
    }

    public OutboxEventEntity(UUID id, String aggregateType, String aggregateId, String eventType, String payload,
            Instant createdAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public static OutboxEventEntityBuilder builder() {
        return new OutboxEventEntityBuilder();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static class OutboxEventEntityBuilder {
        private UUID id;
        private String aggregateType;
        private String aggregateId;
        private String eventType;
        private String payload;
        private Instant createdAt;

        OutboxEventEntityBuilder() {
        }

        public OutboxEventEntityBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public OutboxEventEntityBuilder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public OutboxEventEntityBuilder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public OutboxEventEntityBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public OutboxEventEntityBuilder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public OutboxEventEntityBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OutboxEventEntity build() {
            return new OutboxEventEntity(id, aggregateType, aggregateId, eventType, payload, createdAt);
        }

        public String toString() {
            return "OutboxEventEntity.OutboxEventEntityBuilder(id=" + this.id + ", aggregateType=" + this.aggregateType
                    + ", aggregateId=" + this.aggregateId + ", eventType=" + this.eventType + ", payload="
                    + this.payload + ", createdAt=" + this.createdAt + ")";
        }
    }
}