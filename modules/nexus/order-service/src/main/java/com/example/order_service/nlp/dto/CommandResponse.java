package com.example.order_service.nlp.dto;

import java.util.Map;

/**
 * Command response from NLP parser
 */
public record CommandResponse(
        CommandAction action,
        Map<String, Object> payload,
        String interpretation,
        boolean success) {

    /**
     * Available command actions
     */
    public enum CommandAction {
        FILTER_STATUS, // Filter by order status
        FILTER_LOCATION, // Filter by city/region
        FILTER_RISK, // Filter by risk level
        SWITCH_LENS, // Switch map visualization
        SHOW_STATS, // Display statistics
        CLEAR_FILTERS, // Reset all filters
        UNKNOWN // Command not recognized
    }

    /**
     * Create success response
     */
    public static CommandResponse success(CommandAction action, Map<String, Object> payload, String interpretation) {
        return new CommandResponse(action, payload, interpretation, true);
    }

    /**
     * Create unknown command response
     */
    public static CommandResponse unknown(String query) {
        return new CommandResponse(
                CommandAction.UNKNOWN,
                Map.of("query", query),
                "Command not recognized: " + query,
                false);
    }
}
