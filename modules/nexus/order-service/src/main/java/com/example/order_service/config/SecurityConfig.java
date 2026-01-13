package com.example.order_service.config;

import com.example.order_service.security.OrderJwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ARCHITEKTURA: Finalna wersja konfiguracji bezpieczeństwa dla `order-service`.
 * Została rozbudowana o regułę zabezpieczającą nowe endpointy do zarządzania
 * słownikiem usług dodatkowych.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OrderJwtAuthFilter orderJwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/internal/**").permitAll()
                        .requestMatchers("/api/ingestion/**").permitAll() // Smart Import
                        .requestMatchers("/api/orders/**").permitAll() // MOVED TO TOP
                        .requestMatchers("/api/admin/services/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/orders").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/orders", "/api/orders/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/orders/{orderId}/epod").hasAuthority("ROLE_DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/orders/{orderId}/assign-driver")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/api/orders/{orderId}/status").hasAuthority("ROLE_DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/orders/{orderId}/actions/confirm-pickup")
                        .hasAuthority("ROLE_DRIVER") // NOWA REGUŁA
                        .requestMatchers(HttpMethod.POST, "/api/orders/{orderId}/actions/confirm-delivery")
                        .hasAuthority("ROLE_DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/wms/orders").permitAll() // WMS Integration
                        .requestMatchers("/api/orders/**").permitAll()
                        .requestMatchers("/api/harmonograms/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/scan-events/**").hasAuthority("ROLE_DRIVER")
                        .requestMatchers("/api/vision/**").hasAuthority("ROLE_DRIVER")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(orderJwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        // Allow requests from localhost and external IPs
        configuration.setAllowedOrigins(java.util.List.of(
                "http://localhost:5173",
                "http://localhost:81",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:81",
                "http://91.107.224.0:5173",
                "http://91.107.224.0:81"
        ));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(java.util.List.of("Authorization"));
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}