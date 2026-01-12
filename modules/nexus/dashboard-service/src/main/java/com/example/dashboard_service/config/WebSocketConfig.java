// File: dashboard-service/src/main/java/com/example/dashboard_service/config/WebSocketConfig.java
package com.example.dashboard_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * ARCHITEKTURA: Centralna klasa konfiguracyjna dla WebSocket.
 * Włącza brokera wiadomości STOMP i definiuje endpoint `/ws`, z którym
 * będą łączyć się klienci (frontend). Konfiguruje również prefiksy dla
 * tematów (`/topic`), na które serwer będzie wysyłał aktualizacje.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Włącza prosty broker w pamięci, który będzie rozsyłał wiadomości
        // do klientów subskrybujących tematy z prefiksem /topic
        config.enableSimpleBroker("/topic");
        // Definiuje prefiks dla endpointów, na które klienci mogą wysyłać wiadomości
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Rejestruje endpoint /ws, którego frontend użyje do nawiązania połączenia WebSocket.
        // withSockJS() zapewnia fallback dla przeglądarek, które nie wspierają WebSockets.
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }
}