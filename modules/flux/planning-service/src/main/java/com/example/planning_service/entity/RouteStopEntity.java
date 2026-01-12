package com.example.planning_service.entity;

import com.example.danxils_commons.entity.BaseVoidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Route Stop Entity (Titan).
 * A specific stop on a route, linked to an Order.
 */
@Entity
@Table(name = "vt_route_stop")
@Getter
@Setter
public class RouteStopEntity extends BaseVoidEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private RouteEntity route;

    @Column(name = "order_id")
    private UUID orderId; // Loose coupling to Order Service

    @Embedded
    private LocationPoint location;

    @Column(name = "sequence_number")
    private Integer sequence;

    @Column(name = "predicted_arrival_time")
    private java.time.LocalDateTime predictedArrivalTime;

    @Column(name = "sla_window_start")
    private java.time.LocalDateTime slaWindowStart;

    @Column(name = "sla_window_end")
    private java.time.LocalDateTime slaWindowEnd;
}
