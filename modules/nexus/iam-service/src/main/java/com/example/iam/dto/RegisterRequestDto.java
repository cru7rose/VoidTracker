package com.example.iam.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String companyName;
    private String vatId;
    private String phoneNumber;
    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String country;
    private boolean termsAccepted;
}
