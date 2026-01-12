package com.example.planning_service.config;

import com.example.planning_service.security.PlanningJwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ARCHITEKTURA: Centralna klasa konfiguracyjna Spring Security, rozbudowana
 * o regułę zabezpieczającą nowe endpointy do zarządzania flotą. Dostęp do
 * zasobów `/api/admin/**` jest teraz również ograniczony do administratorów.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final PlanningJwtAuthFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(org.springframework.security.config.Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/planning/plans/**").hasAuthority("ROLE_ADMIN")
                                                .requestMatchers("/api/planning/optimization/**")
                                                .hasAuthority("ROLE_ADMIN")
                                                .requestMatchers("/api/planning/profiles/**").hasAuthority("ROLE_ADMIN")
                                                .requestMatchers("/api/planning/vehicles/**").hasAuthority("ROLE_ADMIN")
                                                .requestMatchers("/api/admin/fleet/vehicles/**")
                                                .hasAuthority("ROLE_ADMIN")
                                                .requestMatchers("/api/driver/auth/**").permitAll()
                                                .requestMatchers("/api/driver/**").authenticated()
                                                .requestMatchers("/api/webhooks/**").permitAll()
                                                .requestMatchers("/api/events/**").permitAll()
                                                .requestMatchers("/api/planning/sentinel/**").permitAll()
                                                .requestMatchers("/api/planning/graph/**").permitAll()
                                                .requestMatchers("/api/planning/test/**").permitAll()
                                                .requestMatchers("/actuator/**").permitAll()
                                                .requestMatchers("/ws-planning/**").permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(e -> e.authenticationEntryPoint(
                                                new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                                                                org.springframework.http.HttpStatus.UNAUTHORIZED)))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

}