package com.example.dashboard_service.service;

import com.example.dashboard_service.dto.CommandResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CommandParserService {

    private static final Pattern TRACK_PATTERN = Pattern
            .compile(".*(?:track|find|locate|where is)\\s+(?:driver\\s+)?([a-zA-Z0-9]+).*");

    public CommandResponse parseCommand(String query) {
        String normalizedQuery = query.trim().toLowerCase();
        log.info("Parsing command: {}", normalizedQuery);

        // 1. Navigation: Show Orders
        if (normalizedQuery.matches(".*(show|list|find|open).*order.*")) {
            return new CommandResponse("NAVIGATE", "/internal/orders", "Navigating to Orders...");
        }

        // 2. Navigation: Show Drivers / Map
        if (normalizedQuery.matches(".*(show|list|find|open).*driver.*") || normalizedQuery.contains("live map")) {
            return new CommandResponse("NAVIGATE", "/internal/map", "Navigating to Live Map...");
        }

        // 3. Navigation: Track specific driver
        Matcher trackMatcher = TRACK_PATTERN.matcher(normalizedQuery);
        if (trackMatcher.matches()) {
            String driverId = trackMatcher.group(1).toUpperCase();
            return new CommandResponse("NAVIGATE", "/internal/map?driver=" + driverId,
                    "Locating Driver " + driverId + "...");
        }

        // 4. Navigation: Risk Lens
        if (normalizedQuery.contains("risk") || normalizedQuery.contains("delay")
                || normalizedQuery.contains("problem")) {
            return new CommandResponse("NAVIGATE", "/internal/map?layer=risk", "Activating Risk Lens...");
        }

        // 5. Navigation: Profit Lens
        if (normalizedQuery.contains("profit") || normalizedQuery.contains("revenue")
                || normalizedQuery.contains("money")) {
            return new CommandResponse("NAVIGATE", "/internal/map?layer=profit", "Activating Profit Lens...");
        }

        // Default: Unknown
        return new CommandResponse("MESSAGE", null,
                "I didn't understand that command. Try 'Show orders', 'Track driver D1', or 'Show risk'.");
    }
}
