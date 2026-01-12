package com.example.iam.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CustomerResponseDto {
    private UUID id;
    private String legacyId;
    private String name;
    private String contactInfo;
    private String userId; // Link to User
    private String attributes;
    private java.util.List<AddressDto> addresses;
}
