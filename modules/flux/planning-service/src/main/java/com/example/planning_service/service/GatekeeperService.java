package com.example.planning_service.service;

import com.example.planning_service.domain.timefold.VehicleRoutingSolution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GatekeeperService {

    @Value("${n8n.webhook.url:http://n8n:5678/webhook/optimization-review}")
    private String n8nWebhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public SafetyReport validateSolution(VehicleRoutingSolution current, VehicleRoutingSolution previous) {
        SafetyReport report = new SafetyReport();
        report.setRequiresApproval(false);
        List<String> warnings = new ArrayList<>();

        if (previous == null || previous.getScore() == null || current.getScore() == null) {
            return report;
        }

        // 1. Check Hard Score (Constraints)
        // If hard score degrades (becomes more negative), that's bad but usually
        // handled by solver.
        // If it improves significantly or changes at all, it might be worth noting.
        // Usually we care about Soft Score (Profit/Distance).

        // 2. Check Soft Score Improvement
        // Soft Score is HardSoftBigDecimalScore.
        // index 0 = Hard, index 1 = Soft (usually).
        // Timefold 'getScore().softScore()' returns BigDecimal.

        BigDecimal currentSoft = current.getScore().softScore();
        BigDecimal prevSoft = previous.getScore().softScore();

        // Avoid division by zero
        if (prevSoft.compareTo(BigDecimal.ZERO) != 0) {
            // Improvement % = (Current - Prev) / |Prev| * 100
            BigDecimal diff = currentSoft.subtract(prevSoft);
            BigDecimal pct = diff.divide(prevSoft.abs(), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));

            log.info("Score improvement: {}%", pct);

            // Threshold: 20%
            if (pct.abs().compareTo(BigDecimal.valueOf(20)) > 0) {
                warnings.add("Significant score change detected: " + pct + "%");
                report.setRequiresApproval(true);
                report.setScoreImprovementPercentage(pct.doubleValue());

                triggerN8nWebhook(current, warnings);
            }
        }

        report.setWarnings(warnings);
        return report;
    }

    private void triggerN8nWebhook(VehicleRoutingSolution solution, List<String> warnings) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("solutionId", solution.getOptimizationId());
            payload.put("score", solution.getScore().toString());
            payload.put("hardScore", solution.getScore().hardScore());
            payload.put("softScore", solution.getScore().softScore()); // Profit info
            payload.put("totalProfit", solution.getTotalProfit());
            payload.put("warnings", warnings);
            payload.put("vehicleCount", solution.getVehicles().size());
            payload.put("unassignedStops", solution.getUnassignedStopsCount());

            // Async call or fire-and-forget
            // In real prod, use @Async or message queue. Here simplistic.
            restTemplate.postForObject(n8nWebhookUrl, payload, String.class);
            log.info("Triggered n8n webhook at {} with warnings: {}", n8nWebhookUrl, warnings);

        } catch (Exception e) {
            log.error("Failed to trigger n8n webhook", e);
        }
    }

    @lombok.Data
    public static class SafetyReport {
        private boolean requiresApproval;
        private List<String> warnings;
        private double scoreImprovementPercentage;
    }
}
