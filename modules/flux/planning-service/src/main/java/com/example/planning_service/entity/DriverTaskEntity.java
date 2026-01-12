package com.example.planning_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverTaskEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String driverId; // The ID of the driver (e.g., from IAM or Employee Service)

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String address;

    private Double lat;
    private Double lon;

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED

    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;

    private String orderId;

    // Proof of delivery fields
    private String scannedCode;
    private boolean hasPhoto;
    private boolean hasSignature;

    @ElementCollection
    private java.util.List<String> photos;
}
