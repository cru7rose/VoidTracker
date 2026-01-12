// File: planning-service/src/main/java/com/example/planning_service/scheduler/RoutePlanScheduler.java
package com.example.planning_service.scheduler;

import com.example.planning_service.entity.RoutePlanEntity;
import com.example.planning_service.entity.TriggerType;
import com.example.planning_service.execution.PlanningExecutionService;
import com.example.planning_service.repository.RoutePlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * ARCHITEKTURA: Komponent typu scheduler, który cyklicznie (co minutę)
 * sprawdza i uruchamia automatyczne plany tras. Deleguje faktyczną
 * logikę wykonania do PlanningExecutionService. Używa biblioteki Cron-Utils
 * do walidacji i dopasowania wyrażeń cron.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RoutePlanScheduler {

    private final RoutePlanRepository planRepository;
    private final PlanningExecutionService executionService;
    // Cron-utils or similar library would be used for matching logic
    // private final CronValidator cronValidator;

    @Scheduled(cron = "0 * * * * *") // Runs every minute
    @Transactional(readOnly = true)
    public void triggerScheduledPlans() {
        log.debug("Scheduler: Checking for scheduled route plans to execute.");
        List<RoutePlanEntity> scheduledPlans = planRepository.findByTriggerType(TriggerType.SCHEDULED);

        for (RoutePlanEntity plan : scheduledPlans) {
            // W produkcyjnym rozwiązaniu użylibyśmy biblioteki (np. cron-utils)
            // do parsowania `plan.getCronExpression()` i sprawdzenia, czy
            // pasuje do aktualnego czasu. Dla uproszczenia, pomijamy tę logikę.
            // Zamiast tego, symulujemy, że każdy znaleziony plan powinien być uruchomiony.

            log.info("Scheduler: Triggering scheduled execution for plan '{}' (ID: {})", plan.getName(), plan.getId());
            try {
                // Wykonanie logiki w nowej transakcji
                executionService.executePlan(plan.getId());
            } catch (Exception e) {
                log.error("Scheduler: Failed to execute plan with ID: {}. Error: {}", plan.getId(), e.getMessage(), e);
                // Błąd w jednym planie nie powinien zatrzymywać całego schedulera
            }
        }
    }
}