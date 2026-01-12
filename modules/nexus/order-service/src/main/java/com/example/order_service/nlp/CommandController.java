package com.example.order_service.nlp;

import com.example.order_service.nlp.dto.CommandRequest;
import com.example.order_service.nlp.dto.CommandResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Oracle NLP Command Controller
 * Endpoint for natural language command parsing
 */
@RestController
@RequestMapping("/api/nlp/command")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CommandController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommandController.class);

    private final CommandParserService parserService;

    /**
     * Parse natural language command
     * 
     * @param request Command query and locale
     * @return Structured command action with payload
     */
    @PostMapping("/parse")
    public ResponseEntity<CommandResponse> parseCommand(@RequestBody CommandRequest request) {
        log.info("Received command: '{}' (locale: {})", request.query(), request.locale());

        try {
            CommandResponse response = parserService.parse(request);

            log.info("Command parsed successfully: action={}, success={}",
                    response.action(), response.success());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Command parsing failed", e);

            return ResponseEntity
                    .internalServerError()
                    .body(new CommandResponse(
                            CommandResponse.CommandAction.UNKNOWN,
                            java.util.Map.of("error", e.getMessage()),
                            "Internal error: " + e.getMessage(),
                            false));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Oracle NLP Service is running");
    }
}
