package com.example.iam.config;

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
            private String usersCreated;
            private String usersUpdated;
            private String usersDeleted;
        }
    }
}