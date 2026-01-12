package com.example.order_service.config;

import org.springframework.context.annotation.Configuration;

/**
 * ARCHITEKTURA: Uproszczona klasa konfiguracyjna Kafki.
 * Usunięto całą ręczną konfigurację fabryk konsumentów. Teraz w 100% polegamy
 * na auto-konfiguracji Spring Boot, która tworzy komponenty na podstawie właściwości
 * w pliku application.yml. To nowoczesne i zalecane podejście, które eliminuje
 * błędy konfiguracyjne i zapewnia poprawne działanie z Testcontainers.
 */
@Configuration
public class KafkaConsumerConfig {

}