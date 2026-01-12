package com.example.order_service.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;

import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Encja JPA reprezentująca wynik wykonanej usługi dodatkowej.
 * Przechowuje referencje do zlecenia i definicji usługi, a także
 * elastyczny payload z wynikiem w formacie JSONB.
 */
@Entity
@Table(name = "order_service_results")
public class OrderServiceResultEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private AdditionalServiceEntity service;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultStatus status;

    @Type(JsonType.class)
    @Column(name = "result_payload", columnDefinition = "jsonb")
    private Object resultPayload;

    @Column(nullable = false)
    private Instant createdAt;

    public enum ResultStatus {
        COMPLETED,
        FAILED,
        SKIPPED
    }

    public OrderServiceResultEntity() {
    }

    public OrderServiceResultEntity(UUID id, OrderEntity order, AdditionalServiceEntity service, String userId,
            ResultStatus status, Object resultPayload, Instant createdAt) {
        this.id = id;
        this.order = order;
        this.service = service;
        this.userId = userId;
        this.status = status;
        this.resultPayload = resultPayload;
        this.createdAt = createdAt;
    }

    public static OrderServiceResultEntityBuilder builder() {
        return new OrderServiceResultEntityBuilder();
    }

    public static class OrderServiceResultEntityBuilder {
        private UUID id;
        private OrderEntity order;
        private AdditionalServiceEntity service;
        private String userId;
        private ResultStatus status;
        private Object resultPayload;
        private Instant createdAt;

        OrderServiceResultEntityBuilder() {
        }

        public OrderServiceResultEntityBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public OrderServiceResultEntityBuilder order(OrderEntity order) {
            this.order = order;
            return this;
        }

        public OrderServiceResultEntityBuilder service(AdditionalServiceEntity service) {
            this.service = service;
            return this;
        }

        public OrderServiceResultEntityBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public OrderServiceResultEntityBuilder status(ResultStatus status) {
            this.status = status;
            return this;
        }

        public OrderServiceResultEntityBuilder resultPayload(Object resultPayload) {
            this.resultPayload = resultPayload;
            return this;
        }

        public OrderServiceResultEntityBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OrderServiceResultEntity build() {
            return new OrderServiceResultEntity(id, order, service, userId, status, resultPayload, createdAt);
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public AdditionalServiceEntity getService() {
        return service;
    }

    public void setService(AdditionalServiceEntity service) {
        this.service = service;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

    public Object getResultPayload() {
        return resultPayload;
    }

    public void setResultPayload(Object resultPayload) {
        this.resultPayload = resultPayload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}