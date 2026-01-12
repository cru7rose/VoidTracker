package com.example.iam.dto;

import lombok.Data;

@Data
public class UpdateEmployeeRequestDto {
    private String department;
    private String jobTitle;
    private String legacyId;
    private String attributes;
}
