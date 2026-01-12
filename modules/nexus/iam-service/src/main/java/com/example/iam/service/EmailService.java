package com.example.iam.service;

public interface EmailService {
    void sendRegistrationEmail(String to, String token);

    void sendPasswordResetEmail(String to, String token);
}