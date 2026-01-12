package com.example.crm_service.entity;

import com.example.danxils_commons.entity.BaseVoidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "vt_client_feedback")
@Getter
@Setter
public class ClientFeedbackEntity extends BaseVoidEntity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "rating")
    private Integer rating; // 1-5

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}
