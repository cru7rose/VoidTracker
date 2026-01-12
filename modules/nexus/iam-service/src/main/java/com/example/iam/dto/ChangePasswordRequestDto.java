package com.example.iam.dto;

import lombok.Data;

/**
 * DTO used for changing a user's password.
 */
@Data
public class ChangePasswordRequestDto {
    private String oldPassword;
    private String newPassword;
}
