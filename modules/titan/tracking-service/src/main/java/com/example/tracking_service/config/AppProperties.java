package com.example.tracking_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * ARCHITEKTURA: Centralna klasa konfiguracyjna, mapująca właściwości z application.yml
 * na obiekt Javy. Zapewnia typowane i bezpieczne zarządzanie konfiguracją,
 * taką jak nazwy tematów Kafka.
 */
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
            private String driverLocationUpdated;
        }
    }
}