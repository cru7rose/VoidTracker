package com.example.crm_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/crm/public/**").permitAll()
                        .requestMatchers("/api/crm/preferences/**").permitAll() // For testing/internal use
                        .requestMatchers("/api/crm/stats/**").permitAll() // Dashboard access
                        .anyRequest().authenticated());
        // Note: JWT Filter should be added here once we bring in the common security
        // libs properly
        // For now, relying on Gateway or internal trust for first pass,
        // OR we need to copy the JwtAuthFilter shim like in planning-service if we want
        // standalone auth.

        return http.build();
    }
}
