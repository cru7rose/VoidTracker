package com.example.planning_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * ARCHITEKTURA: Zero-Hardcoded Configuration for Auto-Planning System
 * 
 * Wszystkie parametry optymalizacji i planowania tras są konfigurowane przez
 * YAML,
 * bez żadnych wartości zhardkodowanych w kodzie. Umożliwia to łatwą adaptację
 * do różnych scenariuszy biznesowych bez rekompilacji.
 * 
 * User Requirement: "musi byc konfigurowalne, zero hardcodow"
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auto-plan")
public class AutoPlanProperties {

    /**
     * Czy auto-planowanie jest włączone (można wyłączyć w środowiskach testowych)
     */
    private boolean enabled = true;

    /**
     * Minimalna liczba zamówień wyzwalająca optymalizację
     * Mniejsze batche = szybsza reakcja, większe = lepsza jakość rozwiązania
     */
    private int minBatchSize = 10;

    /**
     * Maksymalna liczba zamówień w jednym batchu
     * Powyżej tego limitu, batch jest dzielony na mniejsze porcje
     */
    private int maxBatchSize = 1000;

    /**
     * Cron expressions dla scheduled batches
     * Przykład: ["0 0 22 * * *"] = 22:00 każdego dnia
     * Można ustawić wiele: ["0 0 22 * * *", "0 0 2 * * *"] dla 22:00 i 02:00
     */
    private List<String> batchSchedules = new ArrayList<>(List.of("0 0 22 * * *"));

    /**
     * UUID domyślnego profilu optymalizacyjnego dla nocnych dostaw
     * Powinien wskazywać na profil z odpowiednimi wagami constraints (tight time
     * windows)
     */
    private String defaultProfileId;

    /**
     * Czy używać hub-based regional partitioning dla parallel solving
     */
    private boolean regionalPartitioning = false;

    /**
     * Timeout dla synchronicznego przetwarzania batcha (minuty)
     */
    private int batchTimeoutMinutes = 10;

    /**
     * Configuration for hubs (main warehouses and mini-hubs)
     */
    private List<HubConfig> hubs = new ArrayList<>();

    @Data
    public static class HubConfig {
        private String id;
        private String name;
        private HubType type;
        private LocationConfig location;
        private int catchmentRadiusKm;

        @Data
        public static class LocationConfig {
            private double lat;
            private double lon;
        }

        public enum HubType {
            MAIN_HUB,
            MIDI_HUB,
            MICRO_HUB
        }
    }

    /**
     * Urgent order re-optimization configuration
     */
    private UrgentConfig urgent = new UrgentConfig();

    @Data
    public static class UrgentConfig {
        /**
         * Czy automatycznie re-optimize przy urgent orders
         */
        private boolean autoReoptimize = true;

        /**
         * Timeout dla urgent re-optimization (sekundy)
         */
        private int timeoutSeconds = 30;

        /**
         * Czy wymagać manual approval przed zastosowaniem urgent re-optimization
         */
        private boolean requireManualApproval = true;
    }

    /**
     * SLA monitoring configuration
     */
    private SlaConfig sla = new SlaConfig();

    @Data
    public static class SlaConfig {
        /**
         * Ile godzin przed deadline wysyłać WARNING alert (np. 2h před 7 AM = 5 AM)
         */
        private int warningHoursBeforeDeadline = 2;

        /**
         * Ile godzin przed deadline wysyłać CRITICAL alert
         */
        private int criticalHoursBeforeDeadline = 1;

        /**
         * Jak często sprawdzać SLA compliance (minuty)
         */
        private int checkFrequencyMinutes = 15;

        /**
         * Czy wysyłać customer email notifications
         */
        private boolean customerEmailEnabled = true;

        /**
         * Czy wysyłać SMS notifications (requires n8n integration)
         */
        private boolean customerSmsEnabled = false;
    }
}
