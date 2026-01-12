package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "iam_employees")
@Data
@NoArgsConstructor
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "legacy_id")
    private String legacyId;

    private String department;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(columnDefinition = "TEXT")
    private String attributes; // JSON string for dynamic configuration (license, vehicle, etc.)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private UserEntity user;
}
