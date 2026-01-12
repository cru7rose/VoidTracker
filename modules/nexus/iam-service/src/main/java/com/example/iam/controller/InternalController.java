package com.example.iam.controller;

import com.example.danxils_commons.security.JwtUtil;
import com.example.iam.dto.TokenValidationRequestDto;
import com.example.iam.dto.TokenValidationResponseDto;
import com.example.iam.dto.UserDetailsDto;
import com.example.iam.entity.UserEntity;
import com.example.iam.repository.UserRepository;
import com.example.iam.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kontroler REST udostępniający wewnętrzne, niewystawione
 * publicznie
 * endpointy do komunikacji service-to-service. Jego zasoby muszą być chronione
 * na poziomie infrastruktury. Został zaktualizowany, aby używać
 * scentralizowanego
 * komponentu JwtUtil z modułu danxils-commons, zgodnie z zasadą DRY.
 */
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponseDto> validateToken(@RequestBody TokenValidationRequestDto request) {
        String token = request.getToken();
        try {
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("Otrzymano do walidacji wygasły token.");
                return ResponseEntity.ok(TokenValidationResponseDto.builder().valid(false).build());
            }

            String username = jwtUtil.extractUsername(token);
            Optional<UserEntity> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                log.error("Token jest poprawny składniowo, ale odnosi się do nieistniejącego użytkownika: {}",
                        username);
                return ResponseEntity.ok(TokenValidationResponseDto.builder().valid(false).build());
            }

            UserEntity user = userOpt.get();
            Claims claims = jwtUtil.extractAllClaims(token);

            // Extract roles from claims as they capture the context at login time
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            TokenValidationResponseDto response = TokenValidationResponseDto.builder()
                    .valid(true)
                    .userId(user.getUserId().toString())
                    .username(user.getUsername())
                    // Mapping String roles back to UserRole enum if needed, or changing DTO to
                    // accept strings.
                    // Assuming DTO expects Set<UserRole>, we need to map.
                    // However, UserRole enum might be deprecated.
                    // For now, let's try to map strings to UserRole if possible, or empty if not.
                    // Ideally TokenValidationResponseDto should be updated to use Set<String>.
                    // I will assume for now we can map them or I should update the DTO.
                    // Let's check TokenValidationResponseDto.
                    .roles(roles != null ? new java.util.HashSet<>(roles) : java.util.Collections.emptySet())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Próba walidacji nieprawidłowego tokenu JWT. Błąd: {}", e.getMessage());
            // Zwracamy odpowiedź z flagą `valid: false` zamiast błędu 4xx/5xx,
            // ponieważ z perspektywy serwisu wywołującego, "nieprawidłowy token" jest
            // oczekiwanym, poprawnym wynikiem operacji walidacji.
            return ResponseEntity.ok(TokenValidationResponseDto.builder().valid(false).build());
        }
    }

    @PostMapping("/users/details")
    public ResponseEntity<List<UserDetailsDto>> getUserDetails(@RequestBody List<UUID> userIds) {
        return ResponseEntity.ok(userService.findUserDetailsByIds(userIds));
    }
}