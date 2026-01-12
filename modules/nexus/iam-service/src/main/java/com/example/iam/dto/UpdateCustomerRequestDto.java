package com.example.iam.dto;

import lombok.Data;

@Data
public class UpdateCustomerRequestDto {
    private String name;
    private String contactInfo;
    private String legacyId;
    private String attributes;
}
