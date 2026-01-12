// File: analytics-service/src/main/java/com/example/analytics_service/config/AppProperties.java
package com.example.analytics_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * ARCHITEKTURA: Centralna klasa konfiguracyjna, mapująca właściwości z application.yml
 * na obiekt Javy. Zapewnia typowane i bezpieczne zarządzanie konfiguracją.
 */
@Component
@ConfigurationProperties(prefix = "app")
@Data
@Validated
public class AppProperties {

    private final Kafka kafka = new Kafka();

    @Data
    public static class Kafka {
        private final Topics topics = new Topics();

        @Data
        public static class Topics {
            // Tematy główne (wstrzykiwane przez SpEL w @KafkaListener)
            private String ordersCreated;
            private String orderStatusChanged;
            private String ordersAssigned;
            private String routePlanned;
            private String driverLocationUpdated;

            // Tematy DLT (używane w konfiguracji ErrorHandler)
            private String ordersCreatedDlt;
            private String orderStatusChangedDlt;
            private String ordersAssignedDlt;
            private String routePlannedDlt;
            private String driverLocationUpdatedDlt;
        }
    }
}