package com.example.dashboard_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * ARCHITEKTURA: Globalna konfiguracja dla klientów Feign.
 * Definiuje interceptor, który automatycznie przechwytuje nagłówek "Authorization"
 * z przychodzącego żądania HTTP i propaguje go do wszystkich wychodzących wywołań Feign.
 * To eliminuje potrzebę ręcznego dodawania @RequestHeader w każdym kliencie i metodzie.
 */
@Configuration
public class FeignClientConfig {
    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return template -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String authorizationHeader = attributes.getRequest().getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    template.header("Authorization", authorizationHeader);
                }
            }
        };
    }
}