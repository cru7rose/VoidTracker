package com.example.admin_panel_service.client;

import com.example.admin_panel_service.client.fallback.IamServiceClientFallback;
import com.example.admin_panel_service.dto.CreateUserRequestDto;
import com.example.admin_panel_service.dto.UpdateUserRequestDto;
import com.example.admin_panel_service.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Deklaratywny klient Feign do bezpiecznej komunikacji z iam-service.
 * Został oczyszczony z ręcznego przekazywania nagłówka 'Authorization'.
 * Propagacja tokenu JWT jest teraz w pełni zarządzana przez globalny FeignClientConfig,
 * co jest zgodne z zasadą DRY (Don't Repeat Yourself).
 */
@FeignClient(name = "iam-app", fallback = IamServiceClientFallback.class)
public interface IamServiceClient {

    @GetMapping("/api/users")
    List<UserResponseDto> getAllUsers();

    @GetMapping("/api/users/{userId}")
    UserResponseDto getUserById(@PathVariable("userId") UUID userId);

    @PostMapping("/api/users")
    UserResponseDto createUser(@RequestBody CreateUserRequestDto request);

    @PutMapping("/api/users/{userId}")
    UserResponseDto updateUser(@PathVariable("userId") UUID userId, @RequestBody UpdateUserRequestDto request);

    @DeleteMapping("/api/users/{userId}")
    void deleteUser(@PathVariable("userId") UUID userId);
}