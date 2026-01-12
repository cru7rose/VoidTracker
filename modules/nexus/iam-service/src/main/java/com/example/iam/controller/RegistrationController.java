package com.example.iam.controller;

import com.example.iam.dto.CompleteRegistrationRequest;
import com.example.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ARCHITEKTURA: Publicznie dostępny kontroler dedykowany do finalizacji procesu rejestracji.
 * Jego jedynym zadaniem jest obsługa żądania z tokenem i hasłem, co pozwala na aktywację konta.
 * Endpoint ten jest wyłączony z ogólnych reguł bezpieczeństwa w SecurityConfig.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @PostMapping("/complete-registration")
    public ResponseEntity<String> completeRegistration(@RequestBody CompleteRegistrationRequest request) {
        try {
            userService.completeRegistration(request.getToken(), request.getPassword());
            return ResponseEntity.ok("Rejestracja zakończona pomyślnie.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}