package com.example.planning_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry the greeting messages
        // back to the client on destinations prefixed with "/topic"
        config.enableSimpleBroker("/topic");
        // Designates the "/app" prefix for messages that are bound for
        // @MessageMapping-annotated methods.
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws-planning" endpoint, enabling the SockJS protocol options.
        // Allow all origins for simplicity in this internal tool environment.
        registry.addEndpoint("/ws-planning")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
