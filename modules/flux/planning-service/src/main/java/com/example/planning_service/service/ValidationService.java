package com.example.planning_service.service;

import com.example.planning_service.entity.BusinessRuleEntity;
import com.example.planning_service.repository.BusinessRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidationService {

    private final BusinessRuleRepository ruleRepository;

    public boolean validateOrder(Map<String, Object> orderData) {
        List<BusinessRuleEntity> rules = ruleRepository.findAll();
        for (BusinessRuleEntity rule : rules) {
            if (!evaluateRule(rule, orderData)) {
                log.warn("Order failed validation rule: {}", rule.getName());
                return false;
            }
        }
        return true;
    }

    private boolean evaluateRule(BusinessRuleEntity rule, Map<String, Object> data) {
        // Simple mock evaluation logic for now.
        // In a real system, this would use SpEL or a rule engine.
        if ("CAPACITY_CHECK".equals(rule.getRuleType())) {
            // Mock: Check if weight < 1000
            if (data.containsKey("weight")) {
                double weight = Double.parseDouble(data.get("weight").toString());
                return weight < 1000;
            }
        }
        return true;
    }
}
