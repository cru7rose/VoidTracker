package com.example.planning_service.service;

import com.example.planning_service.domain.timefold.VehicleRoutingSolution;
import com.example.planning_service.dto.GatekeeperApprovalRequestDto;
import com.example.planning_service.dto.GatekeeperApprovalResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gatekeeper Service - AI Agent Approval Flow
 * 
 * Monitors optimization solutions and triggers n8n webhook when:
 * - Solution score changes by >20%
 * - Solution requires external marketplace publication
 * - Significant route modifications detected
 * 
 * Workflow:
 * 1. Timefold generates BestSolution
 * 2. Gatekeeper validates against previous solution
 * 3. If threshold exceeded -> Trigger n8n Webhook
 * 4. AI Agent (LLM) generates Polish justification
 * 5. User approves/rejects in Vue UI
 * 6. Only after approval -> Publish to driver
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GatekeeperService {

    private final RestTemplate restTemplate;

    @Value("${gatekeeper.n8n.webhook.url:http://localhost:5678/webhook/gatekeeper}")
    private String n8nWebhookUrl;

    @Value("${gatekeeper.score-threshold-percent:20.0}")
    private double scoreThresholdPercent;

    @Value("${gatekeeper.enabled:true}")
    private boolean enabled;

    /**
     * Safety Report - Result of solution validation
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class SafetyReport {
        private boolean requiresApproval;
        private List<String> warnings;
        private double scoreChangePercent;
        private String justification; // AI-generated Polish justification
        private String approvalId; // Unique ID for approval flow
    }

    /**
     * Validate solution against previous solution
     * Returns SafetyReport indicating if approval is required
     */
    public SafetyReport validateSolution(VehicleRoutingSolution currentSolution, VehicleRoutingSolution previousSolution) {
        if (!enabled) {
            return SafetyReport.builder()
                    .requiresApproval(false)
                    .warnings(new ArrayList<>())
                    .scoreChangePercent(0.0)
                    .build();
        }

        if (previousSolution == null || currentSolution == null) {
            log.debug("No previous solution or current solution is null - skipping validation");
            return SafetyReport.builder()
                    .requiresApproval(false)
                    .warnings(new ArrayList<>())
                    .scoreChangePercent(0.0)
                    .build();
        }

        // Calculate score change percentage
        double scoreChangePercent = calculateScoreChangePercent(currentSolution, previousSolution);
        
        List<String> warnings = new ArrayList<>();
        boolean requiresApproval = false;

        // Check threshold
        if (Math.abs(scoreChangePercent) > scoreThresholdPercent) {
            requiresApproval = true;
            String direction = scoreChangePercent > 0 ? "pogorszenie" : "poprawa";
            warnings.add(String.format("Znacząca zmiana wyniku: %.2f%% (%s)", Math.abs(scoreChangePercent), direction));
            log.warn("⚠️ Gatekeeper: Score change {}% exceeds threshold {}%", 
                    Math.abs(scoreChangePercent), scoreThresholdPercent);
        }

        // Check for route count changes
        int currentRoutes = currentSolution.getVehicles() != null ? currentSolution.getVehicles().size() : 0;
        int previousRoutes = previousSolution.getVehicles() != null ? previousSolution.getVehicles().size() : 0;
        if (Math.abs(currentRoutes - previousRoutes) > 0) {
            warnings.add(String.format("Zmiana liczby tras: %d -> %d", previousRoutes, currentRoutes));
            if (Math.abs(currentRoutes - previousRoutes) > 2) {
                requiresApproval = true;
            }
        }

        // Generate approval ID if approval required
        String approvalId = null;
        String justification = null;
        
        if (requiresApproval) {
            approvalId = generateApprovalId(currentSolution);
            // Trigger n8n webhook for AI justification
            justification = triggerN8nWebhook(currentSolution, previousSolution, scoreChangePercent, warnings);
        }

        return SafetyReport.builder()
                .requiresApproval(requiresApproval)
                .warnings(warnings)
                .scoreChangePercent(scoreChangePercent)
                .justification(justification)
                .approvalId(approvalId)
                .build();
    }

    /**
     * Calculate score change percentage between solutions
     */
    private double calculateScoreChangePercent(VehicleRoutingSolution current, VehicleRoutingSolution previous) {
        if (current.getScore() == null || previous.getScore() == null) {
            return 0.0;
        }

        try {
            // Extract soft score (profit) for comparison
            String currentScoreStr = current.getScore().toString();
            String previousScoreStr = previous.getScore().toString();
            
            // Parse "0hard/1234.56soft" format
            BigDecimal currentSoft = extractSoftScore(currentScoreStr);
            BigDecimal previousSoft = extractSoftScore(previousScoreStr);
            
            if (previousSoft.compareTo(BigDecimal.ZERO) == 0) {
                return currentSoft.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
            }
            
            BigDecimal change = currentSoft.subtract(previousSoft);
            BigDecimal percentChange = change.divide(previousSoft.abs(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            double result = percentChange.doubleValue();
            log.debug("Score change: {}% (current: {}, previous: {})", result, currentSoft, previousSoft);
            return result;
        } catch (Exception e) {
            log.error("Failed to calculate score change", e);
            return 0.0;
        }
    }

    /**
     * Extract soft score from Timefold score string
     */
    private BigDecimal extractSoftScore(String scoreStr) {
        if (scoreStr == null || !scoreStr.contains("/")) {
            return BigDecimal.ZERO;
        }
        try {
            String softPart = scoreStr.split("/")[1].replace("soft", "").trim();
            return new BigDecimal(softPart);
        } catch (Exception e) {
            log.warn("Failed to parse score: {}", scoreStr);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Generate unique approval ID
     */
    private String generateApprovalId(VehicleRoutingSolution solution) {
        return "GK-" + solution.getOptimizationId() + "-" + System.currentTimeMillis();
    }

    /**
     * Trigger n8n webhook for AI Agent justification
     * 
     * Payload sent to n8n:
     * {
     *   "optimizationId": "...",
     *   "scoreChangePercent": 25.5,
     *   "warnings": ["..."],
     *   "currentSolution": {...},
     *   "previousSolution": {...}
     * }
     * 
     * n8n workflow should:
     * 1. Call LLM API (OpenAI/Anthropic) with context
     * 2. Generate Polish justification
     * 3. Return justification in response
     */
    private String triggerN8nWebhook(VehicleRoutingSolution current, VehicleRoutingSolution previous, 
                                    double scoreChangePercent, List<String> warnings) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("optimizationId", current.getOptimizationId());
            payload.put("scoreChangePercent", scoreChangePercent);
            payload.put("warnings", warnings);
            payload.put("routeCount", current.getVehicles() != null ? current.getVehicles().size() : 0);
            payload.put("stopCount", current.getStops() != null ? current.getStops().size() : 0);
            payload.put("timestamp", System.currentTimeMillis());
            
            // Simplified solution summary (avoid sending full objects)
            Map<String, Object> solutionSummary = new HashMap<>();
            solutionSummary.put("currentScore", current.getScore() != null ? current.getScore().toString() : "N/A");
            solutionSummary.put("previousScore", previous.getScore() != null ? previous.getScore().toString() : "N/A");
            payload.put("solutionSummary", solutionSummary);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            log.info("📡 Triggering n8n webhook for Gatekeeper approval: {}", n8nWebhookUrl);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    n8nWebhookUrl != null ? n8nWebhookUrl : "http://localhost:5678/webhook/gatekeeper",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                Object justificationObj = responseBody.get("justification");
                if (justificationObj != null) {
                    String justification = justificationObj.toString();
                    if (!justification.isEmpty()) {
                        log.info("✅ Received AI justification from n8n: {}", justification.substring(0, Math.min(100, justification.length())));
                        return justification;
                    }
                }
            }
            
            // Fallback: Generate simple justification if n8n fails
            return generateFallbackJustification(scoreChangePercent, warnings);
            
        } catch (Exception e) {
            log.error("❌ Failed to trigger n8n webhook for Gatekeeper", e);
            // Fallback justification
            return generateFallbackJustification(scoreChangePercent, warnings);
        }
    }

    /**
     * Generate fallback justification if n8n/LLM is unavailable
     */
    private String generateFallbackJustification(double scoreChangePercent, List<String> warnings) {
        StringBuilder sb = new StringBuilder();
        sb.append("Wykryto znaczącą zmianę w optymalizacji tras. ");
        
        if (scoreChangePercent > 0) {
            sb.append(String.format("Wynik pogorszył się o %.2f%%. ", scoreChangePercent));
        } else {
            sb.append(String.format("Wynik poprawił się o %.2f%%. ", Math.abs(scoreChangePercent)));
        }
        
        if (!warnings.isEmpty()) {
            sb.append("Ostrzeżenia: ").append(String.join(", ", warnings));
        }
        
        sb.append(" Wymagana ręczna weryfikacja przed publikacją.");
        return sb.toString();
    }

    /**
     * Process user approval/rejection
     * Called from Vue UI when user clicks Confirm/Reject
     */
    public GatekeeperApprovalResponseDto processApproval(GatekeeperApprovalRequestDto request) {
        log.info("Processing Gatekeeper approval: approvalId={}, approved={}", 
                request.getApprovalId(), request.isApproved());
        
        if (request.isApproved()) {
            // Approval granted - solution can be published
            return GatekeeperApprovalResponseDto.builder()
                    .approved(true)
                    .message("Rozwiązanie zatwierdzone. Można publikować do kierowców.")
                    .build();
        } else {
            // Approval rejected - solution should not be published
            return GatekeeperApprovalResponseDto.builder()
                    .approved(false)
                    .message("Rozwiązanie odrzucone. Optymalizacja nie zostanie opublikowana.")
                    .build();
        }
    }
}
