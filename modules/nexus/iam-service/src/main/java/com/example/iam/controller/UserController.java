package com.example.iam.controller;

import com.example.iam.dto.ChangePasswordRequestDto;
import com.example.iam.dto.InitiateRegistrationRequest;
import com.example.iam.dto.UpdateUserRequestDto;
import com.example.iam.dto.UserResponseDto;
import com.example.iam.entity.UserEntity;
import com.example.iam.mapper.UserMapper;
import com.example.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Kontroler REST odpowiedzialny za administracyjne zarządzanie
 * użytkownikami.
 * Dostęp do jego zasobów jest ściśle ograniczony do ról administracyjnych, co
 * jest
 * konfigurowane w centralnej klasie SecurityConfig. Został rozbudowany o pełen
 * zestaw operacji CRUD, aby umożliwić kompleksowe zarządzanie użytkownikami
 * z poziomu zewnętrznego panelu administracyjnego.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_USER')")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody InitiateRegistrationRequest request) {
        UserEntity createdUser = userService.initiateRegistration(request);
        return new ResponseEntity<>(userMapper.toResponseDto(createdUser), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId) {
        UserEntity user = userService.findUserById(userId);
        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_USER')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserEntity> users = userService.findAllUsers();
        List<UserResponseDto> dtos = users.stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_USER')")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID userId,
            @RequestBody UpdateUserRequestDto request) {
        UserEntity updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(userMapper.toResponseDto(updatedUser));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_USER')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID userId,
            @RequestBody ChangePasswordRequestDto request) {
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_USER')")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID userId,
            @RequestBody java.util.Map<String, String> request) {
        String newPassword = request.get("password");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        userService.resetPassword(userId, newPassword);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initiate-registration")
    public ResponseEntity<Void> initiateRegistration(@RequestBody InitiateRegistrationRequest request) {
        userService.initiateRegistration(request);
        return ResponseEntity.accepted().build();
    }
}