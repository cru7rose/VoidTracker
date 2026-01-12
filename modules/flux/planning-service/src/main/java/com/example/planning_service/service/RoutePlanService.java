// File: planning-service/src/main/java/com/example/planning_service/service/RoutePlanService.java
package com.example.planning_service.service;

import com.example.planning_service.dto.RoutePlanDto;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Interfejs definiujący kontrakt dla serwisu zarządzającego
 * szablonami planów tras. Abstrakcja ta pozwala na łatwą wymianę implementacji
 * i upraszcza testowanie jednostkowe kontrolerów.
 */
public interface RoutePlanService {

    RoutePlanDto createPlan(RoutePlanDto routePlanDto);

    RoutePlanDto getPlanById(UUID planId);

    List<RoutePlanDto> getAllPlans();

    RoutePlanDto updatePlan(UUID planId, RoutePlanDto routePlanDto);

    void deletePlan(UUID planId);
}