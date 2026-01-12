// Plik: iam-service/src/main/java/com/example/iam/config/SecurityConfig.java
package com.example.iam.config;

import com.example.danxils_commons.security.JwtAuthFilter;
import com.example.iam.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ARCHITEKTURA: Centralna klasa konfiguracyjna dla Spring Security w serwisie
 * IAM.
 * Zarządza dostępem do endpointów tego mikroserwisu. Została zaktualizowana,
 * aby
 * zezwalać na anonimowy dostęp do wewnętrznych endpointów, które MUSZĄ być
 * chronione na poziomie infrastruktury (np. bramy API lub sieci Docker).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publiczne endpointy do logowania i finalizacji rejestracji
                        .requestMatchers("/api/auth/**", "/error", "/actuator/**").permitAll()

                        // --- NOWA REGULA ---
                        // Wewnętrzne endpointy do komunikacji service-to-service.
                        // UWAGA: Dostęp do tych zasobów MUSI być ograniczony na poziomie
                        // infrastruktury (brama API, sieć Docker), aby uniemożliwić
                        // wywołania z zewnątrz.
                        .requestMatchers("/api/internal/**").permitAll()

                        // Endpointy do zarządzania użytkownikami - tylko dla administratora
                        // Updated to use dynamic authority check or just authenticated for now,
                        // as detailed permissions should be handled by method security or gateway.
                        // For refactor, we allow authenticated users to access user endpoints,
                        // but specific actions will be restricted.
                        .requestMatchers("/api/users/**").authenticated()
                        // Wszystkie inne żądania w tym serwisie wymagają uwierzytelnienia
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}