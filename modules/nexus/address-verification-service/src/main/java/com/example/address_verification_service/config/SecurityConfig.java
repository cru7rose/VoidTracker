// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/config/SecurityConfig.java
package com.example.address_verification_service.config;

import com.example.danxils_commons.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ARCHITEKTURA: Centralna konfiguracja Spring Security.
 * Chroni endpointy API do weryfikacji adresów rolami ROLE_ADMIN i ROLE_SUPER_USER.
 * Wymaga wstrzyknięcia JwtAuthFilter i JwtUtil z IAM/Commons.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Włączenie @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Wymagamy autoryzacji dla całego API
                        .requestMatchers("/api/verification/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}