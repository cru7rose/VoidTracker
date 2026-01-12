package com.example.order_service.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vt_chain_of_custody")
public class ChainOfCustodyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String action; // e.g., "CREATED", "PICKUP", "DELIVERED"

    @Column(nullable = false)
    private String actor; // User ID or Driver ID

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private String dataHash; // SHA-256 of the data payload

    @Column(nullable = false)
    private String previousHash; // Hash of the previous block

    @Column(columnDefinition = "text")
    private String signature; // Optional digital signature

    public ChainOfCustodyEntity() {
    }

    public ChainOfCustodyEntity(UUID id, UUID orderId, String action, String actor, Instant timestamp, String dataHash,
            String previousHash, String signature) {
        this.id = id;
        this.orderId = orderId;
        this.action = action;
        this.actor = actor;
        this.timestamp = timestamp;
        this.dataHash = dataHash;
        this.previousHash = previousHash;
        this.signature = signature;
    }

    public static ChainOfCustodyEntityBuilder builder() {
        return new ChainOfCustodyEntityBuilder();
    }

    public static class ChainOfCustodyEntityBuilder {
        private UUID id;
        private UUID orderId;
        private String action;
        private String actor;
        private Instant timestamp;
        private String dataHash;
        private String previousHash;
        private String signature;

        ChainOfCustodyEntityBuilder() {
        }

        public ChainOfCustodyEntityBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ChainOfCustodyEntityBuilder orderId(UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        public ChainOfCustodyEntityBuilder action(String action) {
            this.action = action;
            return this;
        }

        public ChainOfCustodyEntityBuilder actor(String actor) {
            this.actor = actor;
            return this;
        }

        public ChainOfCustodyEntityBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ChainOfCustodyEntityBuilder dataHash(String dataHash) {
            this.dataHash = dataHash;
            return this;
        }

        public ChainOfCustodyEntityBuilder previousHash(String previousHash) {
            this.previousHash = previousHash;
            return this;
        }

        public ChainOfCustodyEntityBuilder signature(String signature) {
            this.signature = signature;
            return this;
        }

        public ChainOfCustodyEntity build() {
            return new ChainOfCustodyEntity(id, orderId, action, actor, timestamp, dataHash, previousHash, signature);
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getDataHash() {
        return dataHash;
    }

    public void setDataHash(String dataHash) {
        this.dataHash = dataHash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
