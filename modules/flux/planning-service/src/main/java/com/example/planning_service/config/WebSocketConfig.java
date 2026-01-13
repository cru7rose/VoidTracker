package com.example.planning_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for Planning Service
 * 
 * Enables real-time updates to frontend during Timefold optimization
 * Endpoint: /ws-planning (STOMP over SockJS)
 * Topics: /topic/optimization-updates (BestSolutionChangedEvent broadcasts)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics (in-memory, suitable for single-instance)
        // For production with multiple instances, use RabbitMQ or Redis broker
        config.enableSimpleBroker("/topic");
        
        // Prefix for application destinations (client -> server)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS fallback
        // Frontend connects to: /ws-planning
        registry.addEndpoint("/ws-planning")
                .setAllowedOriginPatterns("*") // Allow all origins (configure for production)
                .withSockJS(); // Enable SockJS fallback for browsers without WebSocket support
    }
}
