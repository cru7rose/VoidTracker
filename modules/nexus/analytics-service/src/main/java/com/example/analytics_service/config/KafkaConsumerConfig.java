package com.example.analytics_service.config;

import org.springframework.context.annotation.Configuration;

/**
 * ARCHITEKTURA: Uproszczona klasa konfiguracyjna Kafki.
 * Całkowicie polegamy na auto-konfiguracji Spring Boot, która tworzy
 * konsumentów na podstawie właściwości w pliku application.yml.
 * To nowoczesne podejście eliminuje błędy i zapewnia, że dynamiczne
 * właściwości z Testcontainers są poprawnie używane.
 */
@Configuration
public class KafkaConsumerConfig {
    // Celowo pozostawiamy pustą. Spring Boot zrobi resztę.
}