package com.example.iam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private UserInfo user;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private UUID userId;
        private String username;
        private String email;
        private Set<String> roles;
        private java.util.List<OrganizationSummary> organizations;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrganizationSummary {
        private UUID orgId;
        private String legalName;
        private String role; // The role the user has in this org
        private java.util.Map<String, Object> config; // Department restrictions etc.
    }
}