package com.example.iam.service;

import com.example.iam.entity.MagicLinkTokenEntity;
import com.example.iam.entity.UserEntity;
import com.example.iam.repository.MagicLinkTokenRepository;
import com.example.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MagicLinkService {

    private final MagicLinkTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private static final long EXPIRATION_MINUTES = 15;
    private static final long DRIVER_SESSION_DURATION_MS = 12 * 60 * 60 * 1000; // 12 hours

    @Transactional
    public String generateDriverLink(String identifier, String contextId) {
        log.info("Generating magic link for driver: {}", identifier);

        // Upsert User (Shadow User for Driver)
        UserEntity user = userRepository.findByEmail(identifier)
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(identifier);
                    newUser.setUsername(identifier); // Use email/phone as username
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password
                    newUser.setRole(com.example.iam.entity.UserRole.ROLE_DRIVER);
                    // newUser.setTenantId(...) // In real scenario, link to tenant via context
                    return userRepository.save(newUser);
                });

        return generateToken(user);
    }

    private String generateToken(UserEntity user) {
        String token = UUID.randomUUID().toString();
        MagicLinkTokenEntity tokenEntity = new MagicLinkTokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setUserId(user.getUserId());
        tokenEntity.setExpiresAt(Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES));
        tokenEntity.setUsed(false);

        tokenRepository.save(tokenEntity);
        log.info("Generated token: {}", token); // In prod, send via SMS/Email
        return token;
    }

    // Legacy method support if needed, or refactor to use internal generateToken
    @Transactional
    public String generateToken(String email) {
        return generateDriverLink(email, null);
    }

    @Transactional
    public com.example.iam.dto.LoginResponseDto exchangeToken(String token) {
        log.info("Exchanging magic link token");
        MagicLinkTokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (tokenEntity.isUsed()) {
            throw new IllegalArgumentException("Token already used");
        }

        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        tokenEntity.setUsed(true);
        tokenRepository.save(tokenEntity);

        UserEntity user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new IllegalStateException("User associated with token not found"));

        // Generate 12h JWT
        java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.name()))
                .collect(java.util.stream.Collectors.toList());

        String jwt = tokenService.generateToken(user,
                authorities,
                DRIVER_SESSION_DURATION_MS);

        return com.example.iam.dto.LoginResponseDto.builder()
                .accessToken(jwt)
                .refreshToken(jwt) // MVP: reuse same token logic
                .user(com.example.iam.dto.LoginResponseDto.UserInfo.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .roles(user.getRoles().stream().map(r -> r.name())
                                .collect(java.util.stream.Collectors.toSet()))
                        .organizations(java.util.Collections.emptyList()) // Drivers might not need orgs here or linked
                                                                          // later
                        .build())
                .build();
    }
}
