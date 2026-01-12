package com.example.iam.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class EmployeeResponseDto {
    private UUID id;
    private String legacyId;
    private String department;
    private String jobTitle;
    private String userId;
    private String attributes;
}
