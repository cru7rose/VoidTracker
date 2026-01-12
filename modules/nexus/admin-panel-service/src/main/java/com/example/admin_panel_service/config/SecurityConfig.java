package com.example.admin_panel_service.config;

import com.example.danxils_commons.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ARCHITEKTURA: Centralna klasa konfiguracyjna Spring Security dla admin-panel-service.
 * Definiuje politykę bezpieczeństwa dla tego mikroserwisu. Kluczowym zadaniem jest
 * zabezpieczenie wszystkich endpointów administracyjnych (`/api/admin/**`) i
 * wymuszenie, aby dostęp do nich mieli wyłącznie uwierzytelnieni użytkownicy
 * z rolą `ROLE_ADMIN`, co jest weryfikowane na podstawie tokenu JWT.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}