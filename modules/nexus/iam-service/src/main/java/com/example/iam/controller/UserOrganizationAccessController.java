package com.example.iam.controller;

import com.example.iam.entity.UserOrganizationAccess;
import com.example.iam.entity.UserOrgId;
import com.example.iam.repository.UserOrganizationAccessRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ARCHITEKTURA: Kontroler REST odpowiedzialny za zarządzanie dostępem
 * użytkowników do organizacji.
 * Umożliwia aktualizację ról i konfiguracji (JSONB) dla konkretnej pary
 * Użytkownik-Organizacja.
 */
@RestController
@RequestMapping("/api/users/{userId}/organizations/{orgId}")
@RequiredArgsConstructor
public class UserOrganizationAccessController {

    private final UserOrganizationAccessRepository userOrgAccessRepository;

    @PutMapping
    public ResponseEntity<Void> updateAccess(
            @PathVariable UUID userId,
            @PathVariable UUID orgId,
            @RequestBody UpdateAccessRequest request) {

        UserOrgId id = new UserOrgId(userId, orgId);
        UserOrganizationAccess access = userOrgAccessRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalStateException("Access not found for user " + userId + " and org " + orgId));

        if (request.getRoleDefinitionId() != null) {
            access.setRoleDefinitionId(request.getRoleDefinitionId());
        }
        if (request.getConfigOverrides() != null) {
            access.setConfigOverrides(request.getConfigOverrides());
        }

        userOrgAccessRepository.save(access);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Void> createAccess(
            @PathVariable UUID userId,
            @PathVariable UUID orgId,
            @RequestBody UpdateAccessRequest request) {

        UserOrgId id = new UserOrgId(userId, orgId);
        if (userOrgAccessRepository.existsById(id)) {
            throw new IllegalStateException("Access already exists for user " + userId + " and org " + orgId);
        }

        UserOrganizationAccess access = new UserOrganizationAccess();
        access.setId(id);
        access.setRoleDefinitionId(request.getRoleDefinitionId() != null ? request.getRoleDefinitionId() : "customer");
        access.setConfigOverrides(request.getConfigOverrides());

        userOrgAccessRepository.save(access);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).build();
    }

    @Data
    public static class UpdateAccessRequest {
        private String roleDefinitionId;
        private String configOverrides;
    }
}
