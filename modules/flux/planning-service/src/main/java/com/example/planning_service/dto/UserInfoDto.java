package com.example.planning_service.dto;

import lombok.Data;
import java.util.Set;

/**
 * DTO for user information from IAM service
 * Simplified version of UserResponseDto for Planning Service use
 */
@Data
public class UserInfoDto {
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
}
