package com.example.order_service.nlp;

import com.example.order_service.nlp.dto.CommandRequest;
import com.example.order_service.nlp.dto.CommandResponse;
import com.example.order_service.nlp.dto.CommandResponse.CommandAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NLP Command Parser Service
 * Uses regex-based pattern matching for intent classification
 * Supports Polish and English commands
 */
@Service
public class CommandParserService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommandParserService.class);

    // Status filter patterns
    private static final Pattern PATTERN_RISK_PL = Pattern.compile(
            ".*(pokaż|wyświetl|filtruj).*(zagrożone|ryzyko|opóźnione|at-risk).*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern PATTERN_RISK_EN = Pattern.compile(
            ".*(show|display|filter).*(at-risk|delayed|risk).*",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_STATUS_PL = Pattern.compile(
            ".*(pokaż|wyświetl|filtruj).*(w trakcie|pickup|nowe|dostarczone).*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern PATTERN_STATUS_EN = Pattern.compile(
            ".*(show|display|filter).*(pickup|new|delivered|in progress).*",
            Pattern.CASE_INSENSITIVE);

    // Location filter pattern
    private static final Pattern PATTERN_LOCATION = Pattern.compile(
            ".*(w|in)\\s+(\\w+).*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    // Lens switch patterns
    private static final Pattern PATTERN_LENS_PROF_PL = Pattern.compile(
            ".*(przełącz|pokaż|zmień).*(profit|zysk|rentowność).*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern PATTERN_LENS_PROF_EN = Pattern.compile(
            ".*(switch|show|change).*(profit|profitability).*",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_LENS_MESH_PL = Pattern.compile(
            ".*(przełącz|pokaż|zmień).*(sieć|void-mesh|trasy).*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern PATTERN_LENS_MESH_EN = Pattern.compile(
            ".*(switch|show|change).*(mesh|void-mesh|routes).*",
            Pattern.CASE_INSENSITIVE);

    // Clear filters patterns
    private static final Pattern PATTERN_CLEAR_PL = Pattern.compile(
            ".*(wyczyść|usuń|reset|clear).*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern PATTERN_CLEAR_EN = Pattern.compile(
            ".*(clear|reset|remove).*(filter|all).*",
            Pattern.CASE_INSENSITIVE);

    // Stats patterns
    private static final Pattern PATTERN_STATS_PL = Pattern.compile(
            ".*(statystyki|stats|pokaż liczby).*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern PATTERN_STATS_EN = Pattern.compile(
            ".*(stats|statistics|show numbers).*",
            Pattern.CASE_INSENSITIVE);

    /**
     * Parse natural language command
     */
    public CommandResponse parse(CommandRequest request) {
        String query = request.query().toLowerCase().trim();
        log.info("Parsing command: '{}' (locale: {})", query, request.locale());

        // Detect intent
        CommandAction action = detectIntent(query);

        if (action == CommandAction.UNKNOWN) {
            log.warn("Unknown command: '{}'", query);
            return CommandResponse.unknown(query);
        }

        // Extract entities based on action
        Map<String, Object> payload = extractEntities(query, action);

        // Build human-readable interpretation
        String interpretation = buildInterpretation(action, payload);

        log.info("Parsed: action={}, payload={}, interpretation='{}'", action, payload, interpretation);

        return CommandResponse.success(action, payload, interpretation);
    }

    /**
     * Detect command intent from query
     */
    private CommandAction detectIntent(String query) {
        // Clear filters (highest priority)
        if (PATTERN_CLEAR_PL.matcher(query).matches() || PATTERN_CLEAR_EN.matcher(query).matches()) {
            return CommandAction.CLEAR_FILTERS;
        }

        // Stats
        if (PATTERN_STATS_PL.matcher(query).matches() || PATTERN_STATS_EN.matcher(query).matches()) {
            return CommandAction.SHOW_STATS;
        }

        // Lens switch
        if (PATTERN_LENS_PROF_PL.matcher(query).matches() || PATTERN_LENS_PROF_EN.matcher(query).matches()) {
            return CommandAction.SWITCH_LENS;
        }
        if (PATTERN_LENS_MESH_PL.matcher(query).matches() || PATTERN_LENS_MESH_EN.matcher(query).matches()) {
            return CommandAction.SWITCH_LENS;
        }

        // Risk/Status filter
        if (PATTERN_RISK_PL.matcher(query).matches() || PATTERN_RISK_EN.matcher(query).matches()) {
            return CommandAction.FILTER_STATUS;
        }
        if (PATTERN_STATUS_PL.matcher(query).matches() || PATTERN_STATUS_EN.matcher(query).matches()) {
            return CommandAction.FILTER_STATUS;
        }

        // Location filter
        if (PATTERN_LOCATION.matcher(query).find()) {
            return CommandAction.FILTER_LOCATION;
        }

        return CommandAction.UNKNOWN;
    }

    /**
     * Extract entities from query based on action type
     */
    private Map<String, Object> extractEntities(String query, CommandAction action) {
        Map<String, Object> payload = new HashMap<>();

        switch (action) {
            case FILTER_STATUS:
                // Extract status
                if (query.contains("zagrożone") || query.contains("at-risk") || query.contains("opóźnione")
                        || query.contains("delayed")) {
                    payload.put("status", "AT_RISK");
                } else if (query.contains("w trakcie") || query.contains("pickup")) {
                    payload.put("status", "PICKUP");
                } else if (query.contains("nowe") || query.contains("new")) {
                    payload.put("status", "NEW");
                } else if (query.contains("dostarczone") || query.contains("delivered")) {
                    payload.put("status", "POD");
                }

                // Also extract location if present
                extractLocation(query, payload);
                break;

            case FILTER_LOCATION:
                extractLocation(query, payload);
                break;

            case SWITCH_LENS:
                // Determine which lens
                if (query.contains("profit") || query.contains("zysk") || query.contains("rentowność")) {
                    payload.put("lens", "profitability");
                } else if (query.contains("mesh") || query.contains("sieć") || query.contains("trasy")) {
                    payload.put("lens", "void-mesh");
                }
                break;

            case CLEAR_FILTERS:
            case SHOW_STATS:
            case UNKNOWN:
                // No entities needed
                break;
        }

        return payload;
    }

    /**
     * Extract city/location from query
     */
    private void extractLocation(String query, Map<String, Object> payload) {
        Matcher matcher = PATTERN_LOCATION.matcher(query);
        if (matcher.find()) {
            String city = matcher.group(2);
            // Normalize common Polish city names
            city = normalizeCity(city);
            payload.put("city", city);
        }
    }

    /**
     * Normalize Polish city names
     */
    private String normalizeCity(String city) {
        return switch (city.toLowerCase()) {
            case "łodzi", "lodzi", "lodz" -> "Lodz";
            case "warszawie", "warszawa", "warsaw" -> "Warsaw";
            case "krakowie", "kraków", "krakow", "cracow" -> "Krakow";
            case "gdańsku", "gdansk" -> "Gdansk";
            case "wrocławiu", "wrocław", "wroclaw" -> "Wroclaw";
            case "poznaniu", "poznań", "poznan" -> "Poznan";
            default -> Character.toUpperCase(city.charAt(0)) + city.substring(1).toLowerCase();
        };
    }

    /**
     * Build human-readable interpretation
     */
    private String buildInterpretation(CommandAction action, Map<String, Object> payload) {
        return switch (action) {
            case FILTER_STATUS -> {
                String status = (String) payload.get("status");
                String city = (String) payload.get("city");
                if (city != null) {
                    yield String.format("Filtering orders: status=%s, city=%s", status, city);
                }
                yield String.format("Filtering orders by status: %s", status);
            }
            case FILTER_LOCATION ->
                String.format("Filtering orders by location: %s", payload.get("city"));
            case SWITCH_LENS ->
                String.format("Switching to lens: %s", payload.get("lens"));
            case CLEAR_FILTERS ->
                "Clearing all filters";
            case SHOW_STATS ->
                "Displaying statistics";
            default ->
                "Unknown command";
        };
    }
}
