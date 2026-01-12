package com.example.planning_service.service;

import com.example.danxils_commons.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlanningService {

    // Legacy logic removed. Planning is now done via OptimizationController
    // (Timefold).
    public void createPlanForNewOrder(OrderCreatedEvent orderEvent) {
        log.info("Ignoring OrderCreatedEvent for legacy planning. Use Unified Dispatch.");
    }
}