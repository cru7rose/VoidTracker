// File: planning-service/src/main/java/com/example/planning_service/entity/TriggerType.java
package com.example.planning_service.entity;

/**
 * ARCHITEKTURA: Enum definiujący możliwe sposoby uruchomienia planu trasy.
 * Zapewnia typowaną i jednoznaczną logikę w serwisach i schedulerach.
 */
public enum TriggerType {
    /**
     * Plan jest uruchamiany wyłącznie ręcznie przez użytkownika (np. przez API).
     */
    MANUAL,
    /**
     * Plan jest uruchamiany automatycznie na podstawie zdefiniowanego harmonogramu (cron).
     */
    SCHEDULED
}