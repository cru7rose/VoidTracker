package com.example.iam.dto;
import lombok.Data;

@Data
public class CompleteRegistrationRequest {
    private String token;
    private String password;
}