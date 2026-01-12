package com.example.iam.controller;

import com.example.iam.dto.LoginRequestDto;
import com.example.iam.dto.LoginResponseDto;
import com.example.iam.entity.UserEntity;
import com.example.iam.repository.UserRepository;
import com.example.iam.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ARCHITEKTURA: Dedykowany kontroler do obsługi procesu uwierzytelniania.
 * Udostępnia publiczny endpoint /api/auth/login, który jest wyłączony z
 * ogólnych reguł
 * bezpieczeństwa. Jego jedynym zadaniem jest wygenerowanie i zwrócenie tokenu
 * JWT.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final UserRepository userRepository;
        private final TokenService tokenService;
        private final com.example.iam.service.MagicLinkService magicLinkService;
        private final com.example.iam.service.UserService userService;

        private final com.example.iam.repository.UserOrganizationAccessRepository userOrgAccessRepository;
        private final com.example.iam.repository.OrganizationRepository organizationRepository;

        @PostMapping("/login")
        public ResponseEntity<LoginResponseDto> createAuthenticationToken(@RequestBody LoginRequestDto loginRequest) {
                org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                                                loginRequest.getPassword()));

                final UserEntity user = userRepository.findByUsername(loginRequest.getUsername())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with username: " + loginRequest.getUsername()));

                return generateLoginResponse(user, authentication.getAuthorities());
        }

        @PostMapping("/register")
        public ResponseEntity<LoginResponseDto> registerUser(
                        @RequestBody com.example.iam.dto.RegisterRequestDto registerRequest) {
                UserEntity user = userService.registerUser(registerRequest);
                return generateLoginResponse(user, java.util.Collections.emptyList());
        }

        @PostMapping("/custom-forgot-password")
        public ResponseEntity<java.util.Map<String, String>> forgotPassword(
                        @RequestBody java.util.Map<String, String> request) {
                String email = request.get("email");
                if (email != null && !email.isBlank()) {
                        try {
                                userService.initiatePasswordReset(email);
                        } catch (jakarta.persistence.EntityNotFoundException e) {
                                // Silently ignore to prevent user enumeration
                        }
                }
                return ResponseEntity.accepted()
                                .body(java.util.Collections.singletonMap("message", "Reset link sent if email exists"));
        }

        @PostMapping("/custom-reset-password")
        public ResponseEntity<Void> resetPassword(@RequestBody java.util.Map<String, String> request) {
                String token = request.get("token");
                String newPassword = request.get("password");
                if (token == null || newPassword == null) {
                        return ResponseEntity.badRequest().build();
                }
                userService.completePasswordReset(token, newPassword);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/change-password")
        public ResponseEntity<Void> changePassword(@RequestBody com.example.iam.dto.ChangePasswordRequestDto request) {
                String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                                .getAuthentication().getName();
                UserEntity user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                userService.changePassword(user.getUserId(), request);
                return ResponseEntity.ok().build();
        }

        private ResponseEntity<LoginResponseDto> generateLoginResponse(UserEntity user,
                        java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
                final String accessToken = tokenService.generateToken(user, authorities);
                final String refreshToken = tokenService.generateToken(user, authorities);

                // Fetch user's organizations
                java.util.List<com.example.iam.entity.UserOrganizationAccess> accesses = userOrgAccessRepository
                                .findByIdUserId(user.getUserId());

                java.util.List<LoginResponseDto.OrganizationSummary> orgSummaries = accesses.stream()
                                .map(access -> {
                                        com.example.iam.entity.Organization org = organizationRepository
                                                        .findById(access.getId().getOrgId())
                                                        .orElse(null);
                                        if (org == null)
                                                return null;

                                        return LoginResponseDto.OrganizationSummary.builder()
                                                        .orgId(org.getOrgId())
                                                        .legalName(org.getLegalName())
                                                        .role(access.getRoleDefinitionId())
                                                        .build();
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(java.util.stream.Collectors.toList());

                LoginResponseDto.UserInfo userInfo = LoginResponseDto.UserInfo.builder()
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .roles(authorities.stream()
                                                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                                                .collect(java.util.stream.Collectors.toSet()))
                                .organizations(orgSummaries)
                                .build();

                LoginResponseDto response = LoginResponseDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .user(userInfo)
                                .build();

                return ResponseEntity.ok(response);
        }
}