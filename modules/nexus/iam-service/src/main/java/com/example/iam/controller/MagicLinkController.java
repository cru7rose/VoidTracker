package com.example.iam.controller;

import com.example.iam.service.MagicLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/magic-link")
@RequiredArgsConstructor
@Tag(name = "Magic Link Auth", description = "Authentication via magic links")
public class MagicLinkController {

    private final MagicLinkService magicLinkService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a magic link token for the given email")
    public ResponseEntity<Map<String, String>> generateToken(@RequestParam String email) {
        String token = magicLinkService.generateToken(email);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/driver/generate")
    @Operation(summary = "Generate a magic link for a driver (creates shadow user if needed)")
    public ResponseEntity<Map<String, String>> generateDriverLink(@RequestParam String identifier, @RequestParam(required = false) String contextId) {
        String token = magicLinkService.generateDriverLink(identifier, contextId);
        String link = "https://app.voidtracker.com/auth/magic?token=" + token;
        return ResponseEntity.ok(Map.of("token", token, "link", link));
    }

    @PostMapping("/exchange")
    @Operation(summary = "Exchange a magic link token for a long-lived JWT")
    public ResponseEntity<com.example.iam.dto.LoginResponseDto> exchangeToken(@RequestParam String token) {
        return ResponseEntity.ok(magicLinkService.exchangeToken(token));
    }
}
