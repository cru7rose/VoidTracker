package com.example.iam.dto;

import lombok.Data;

import java.util.Set;

@Data
public class InitiateRegistrationRequest {
    private String email;
    private Set<String> roles; // Accept role names as strings
    private String fullName;
    private String avatarUrl;
    private String bio;
    private String preferences;
    private java.util.UUID organizationId;
    private String roleDefinitionId;
    private String configOverrides;
    private String username; // Add username field for create
    private Boolean enabled; // Add enabled field
    private String legacyId; // Add legacy ID field
}