package com.example.iam.service;

import com.example.danxils_commons.event.UserCreatedEvent;
import com.example.danxils_commons.event.UserDeletedEvent;
import com.example.danxils_commons.event.UserUpdatedEvent;
import com.example.iam.config.AppProperties;
import com.example.iam.dto.UpdateUserRequestDto;
import com.example.iam.dto.UserDetailsDto;
import com.example.iam.entity.*;
import com.example.iam.mapper.UserMapper;
import com.example.iam.repository.*;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Serwis centralny do zarządzania użytkownikami, rozbudowany o
 * odpowiedzialność publikowania zdarzeń domenowych do Kafki. Każda operacja
 * modyfikująca stan (tworzenie, aktualizacja, usuwanie) kończy się wysłaniem
 * odpowiedniego komunikatu, informując resztę systemu o zmianie.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationAccessRepository userOrgAccessRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationTokenRepository tokenRepository;
    private final com.example.iam.repository.PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final AppProperties appProperties;

    private final CustomerService customerService;

    @Transactional
    public UserEntity registerUser(com.example.iam.dto.RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Użytkownik o takim adresie email już istnieje.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("Użytkownik o takiej nazwie już istnieje.");
        }
        if (!request.isTermsAccepted()) {
            throw new IllegalStateException("Wymagana akceptacja regulaminu.");
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEnabled(true);
        newUser.setGlobalStatus(UserEntity.GlobalStatus.ACTIVE);
        newUser.setFullName(request.getFullName());

        UserEntity savedUser = userRepository.save(newUser);

        // Assign to default organization with customer role
        assignToDefaultOrg(savedUser, "customer");

        // Create Customer Profile
        if (request.getCompanyName() != null || request.getVatId() != null) {
            com.example.iam.dto.UpdateCustomerRequestDto customerProfile = new com.example.iam.dto.UpdateCustomerRequestDto();
            customerProfile
                    .setName(request.getCompanyName() != null ? request.getCompanyName() : request.getFullName());
            customerProfile.setContactInfo(request.getPhoneNumber());
            customerProfile
                    .setAttributes("{\"vatId\": \"" + (request.getVatId() != null ? request.getVatId() : "") + "\"}");
            customerService.createCustomerProfile(savedUser.getUserId(), customerProfile);

            // Add Address if provided
            if (request.getStreet() != null && request.getCity() != null) {
                com.example.iam.dto.AddressDto address = new com.example.iam.dto.AddressDto();
                address.setStreet(request.getStreet());
                address.setHouseNumber(request.getHouseNumber());
                address.setPostalCode(request.getPostalCode());
                address.setCity(request.getCity());
                address.setCountry(request.getCountry());
                address.setType("MAIN");
                customerService.addAddress(savedUser.getUserId(), address);
            }
        }

        publishUserCreatedEvent(savedUser);

        return savedUser;
    }

    @Transactional
    public UserEntity initiateRegistration(com.example.iam.dto.InitiateRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Użytkownik o takim adresie email już istnieje.");
        }

        // Use username from request, fallback to email if not provided
        String username = request.getUsername() != null ? request.getUsername() : request.getEmail();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Użytkownik o takiej nazwie już istnieje.");
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setEnabled(request.getEnabled() != null ? request.getEnabled() : false);
        newUser.setGlobalStatus(UserEntity.GlobalStatus.ACTIVE);
        newUser.setFullName(request.getFullName());
        newUser.setAvatarUrl(request.getAvatarUrl());
        newUser.setPreferences(request.getPreferences());
        newUser.setBio(request.getBio());
        newUser.setLegacyId(request.getLegacyId());

        UserEntity savedUser = userRepository.save(newUser);

        // Assign to organization
        if (request.getOrganizationId() != null) {
            assignToOrganization(savedUser, request.getOrganizationId(), request.getRoleDefinitionId(),
                    request.getConfigOverrides());
        } else {
            // Fallback to default behavior if no org specified
            // Map roles to new role definitions (simplification for migration)
            String roleDefId = "customer";
            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                // Simple mapping logic: get first role and convert from ROLE_DRIVER -> driver
                String firstRole = request.getRoles().iterator().next();
                roleDefId = firstRole.replace("ROLE_", "").toLowerCase();
            }
            assignToDefaultOrg(savedUser, roleDefId);
        }

        RegistrationTokenEntity registrationToken = new RegistrationTokenEntity(savedUser);
        tokenRepository.save(registrationToken);

        try {
            emailService.sendRegistrationEmail(newUser.getEmail(), registrationToken.getToken());
        } catch (Exception e) {
            log.error("Failed to send registration email to {}", newUser.getEmail(), e);
        }

        try {
            publishUserCreatedEvent(savedUser);
        } catch (Exception e) {
            log.error("Failed to publish user created event for {}", newUser.getEmail(), e);
        }

        return savedUser;
    }

    private void assignToDefaultOrg(UserEntity user, String roleId) {
        Organization defaultOrg = organizationRepository.findByLegalName("Danxils Default Org")
                .orElseGet(() -> {
                    log.info("Default organization not found. Creating 'Danxils Default Org'.");
                    Organization newOrg = new Organization();
                    newOrg.setOrgId(UUID.randomUUID());
                    newOrg.setLegalName("Danxils Default Org");
                    newOrg.setVatId("DEFAULT");
                    return organizationRepository.save(newOrg);
                });
        assignToOrganization(user, defaultOrg.getOrgId(), roleId, null);
    }

    private void assignToOrganization(UserEntity user, UUID orgId, String roleId, String configOverrides) {
        UserOrgId accessId = new UserOrgId(user.getUserId(), orgId);
        UserOrganizationAccess access = new UserOrganizationAccess();
        access.setId(accessId);
        access.setRoleDefinitionId(roleId != null ? roleId : "customer");
        access.setConfigOverrides(configOverrides);
        userOrgAccessRepository.save(access);
    }

    @Transactional
    public void completeRegistration(String token, String password) {
        RegistrationTokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Nieprawidłowy token rejestracyjny."));

        if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(tokenEntity);
            throw new IllegalStateException("Token rejestracyjny wygasł.");
        }

        UserEntity user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        userRepository.save(user);

        tokenRepository.delete(tokenEntity);
    }

    @Transactional(readOnly = true)
    public UserEntity findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika o ID: " + userId));
    }

    @Transactional(readOnly = true)
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UserDetailsDto> findUserDetailsByIds(List<UUID> userIds) {
        List<UserEntity> users = userRepository.findAllByUserIdIn(userIds);
        return userMapper.toDetailsDtoList(users);
    }

    @Transactional
    public UserEntity updateUser(UUID userId, UpdateUserRequestDto request) {
        UserEntity user = findUserById(userId);
        UserUpdatedEvent.UserPayload beforePayload = userMapper.toUserPayload(user);

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null\u0026\u0026!request.getEmail().equals(user.getEmail())) {
            // Email is changing - validate uniqueness (excluding current user)
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalStateException("Użytkownik o takim adresie email już istnieje.");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getLegacyId() != null) {
            user.setLegacyId(request.getLegacyId());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getRoles() != null) {
            // Convert string roles to UserRole enum
            java.util.Set<UserRole> roleEnums = request.getRoles().stream()
                    .map(UserRole::valueOf) // Convert string to enum
                    .collect(java.util.stream.Collectors.toSet());
            user.setRoles(roleEnums);
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getPreferences() != null) {
            // Ensure valid JSON - empty string is not valid, use "{}" instead
            String prefs = request.getPreferences().trim();
            user.setPreferences(prefs.isEmpty() ? "{}" : prefs);
        }
        UserEntity updatedUser = userRepository.save(user);

        UserUpdatedEvent.UserPayload afterPayload = userMapper.toUserPayload(updatedUser);
        publishUserUpdatedEvent(updatedUser, beforePayload, afterPayload);

        return updatedUser;
    }

    @Transactional
    public void deleteUser(UUID userId) {
        UserEntity userToDelete = findUserById(userId);

        tokenRepository.deleteByUser_UserId(userId);
        userRepository.deleteById(userId);

        publishUserDeletedEvent(userToDelete);
    }

    @Transactional
    public void changePassword(UUID userId, com.example.iam.dto.ChangePasswordRequestDto request) {
        UserEntity user = findUserById(userId);
        // Verify old password matches
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password does not match");
        }
        // Set new encoded password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        UserEntity updatedUser = userRepository.save(user);
        // Publish updated event
        UserUpdatedEvent.UserPayload before = userMapper.toUserPayload(user);
        UserUpdatedEvent.UserPayload after = userMapper.toUserPayload(updatedUser);
        publishUserUpdatedEvent(updatedUser, before, after);
    }

    public void resetPassword(UUID userId, String newPassword) {
        UserEntity user = findUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        UserEntity updatedUser = userRepository.save(user);

        UserUpdatedEvent.UserPayload before = userMapper.toUserPayload(user);
        UserUpdatedEvent.UserPayload after = userMapper.toUserPayload(updatedUser);
        publishUserUpdatedEvent(updatedUser, before, after);
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Użytkownik z podanym adresem email nie istnieje."));

        // Delete existing token if any
        // Check if token exists for user
        com.example.iam.entity.PasswordResetTokenEntity existingToken = passwordResetTokenRepository
                .findByUser(user)
                .orElse(null);

        if (existingToken != null) {
            passwordResetTokenRepository.delete(existingToken);
            passwordResetTokenRepository.flush(); // Ensure delete is executed before insert
        }
        // Better: delete any tokens for this user via repository custom query if
        // strictly 1:1,
        // but for now Entity is 1:1 so we can just create new one.
        // Actually, let's just create a new one.

        com.example.iam.entity.PasswordResetTokenEntity tokenEntity = new com.example.iam.entity.PasswordResetTokenEntity(
                user);
        passwordResetTokenRepository.save(tokenEntity);

        emailService.sendPasswordResetEmail(user.getEmail(), tokenEntity.getToken());
    }

    @Transactional
    public void completePasswordReset(String token, String newPassword) {
        com.example.iam.entity.PasswordResetTokenEntity tokenEntity = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Nieprawidłowy token resetowania hasła."));

        if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(tokenEntity);
            throw new IllegalStateException("Token resetowania hasła wygasł.");
        }

        UserEntity user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        UserEntity updatedUser = userRepository.save(user); // Save new password

        passwordResetTokenRepository.delete(tokenEntity); // Consume token

        // Audit/Event
        UserUpdatedEvent.UserPayload before = userMapper.toUserPayload(user);
        UserUpdatedEvent.UserPayload after = userMapper.toUserPayload(updatedUser);
        publishUserUpdatedEvent(updatedUser, before, after);
    }

    private void publishUserCreatedEvent(UserEntity user) {
        // Fetch roles for event
        List<UserOrganizationAccess> accessList = userOrgAccessRepository.findByIdUserId(user.getUserId());
        Set<String> roles = accessList.stream().map(UserOrganizationAccess::getRoleDefinitionId)
                .collect(Collectors.toSet());

        UserCreatedEvent event = UserCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(Instant.now())
                .performedBy(getCurrentUsername())
                .userId(user.getUserId())
                .email(user.getEmail())
                .roles(roles)
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .build();
        publishEvent(appProperties.getKafka().getTopics().getUsersCreated(), user.getUserId().toString(), event);
    }

    private void publishUserUpdatedEvent(UserEntity user, UserUpdatedEvent.UserPayload before,
            UserUpdatedEvent.UserPayload after) {
        UserUpdatedEvent event = UserUpdatedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(Instant.now())
                .performedBy(getCurrentUsername())
                .userId(user.getUserId())
                .before(before)
                .after(after)
                .build();
        publishEvent(appProperties.getKafka().getTopics().getUsersUpdated(), user.getUserId().toString(), event);
    }

    private void publishUserDeletedEvent(UserEntity user) {
        UserDeletedEvent event = UserDeletedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(Instant.now())
                .performedBy(getCurrentUsername())
                .userId(user.getUserId())
                .email(user.getEmail())
                .build();
        publishEvent(appProperties.getKafka().getTopics().getUsersDeleted(), user.getUserId().toString(), event);
    }

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "system";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void publishEvent(String topic, String key, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            log.info("Publikacja zdarzenia {} do tematu '{}' z kluczem {}", event.getClass().getSimpleName(), topic,
                    key);
            kafkaTemplate.send(topic, key, payload);
        } catch (Exception e) {
            log.error("KRYTYCZNY BŁĄD: Nie udało się zainicjować wysyłki zdarzenia dla klucza {}.", key, e);
        }
    }
}