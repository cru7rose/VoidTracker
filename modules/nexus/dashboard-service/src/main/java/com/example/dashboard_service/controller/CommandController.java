package com.example.dashboard_service.controller;

import com.example.dashboard_service.dto.CommandRequest;
import com.example.dashboard_service.dto.CommandResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/dashboard/command")
@RequiredArgsConstructor
@Slf4j
public class CommandController {

    private final com.example.dashboard_service.service.CommandParserService commandParserService;

    @PostMapping
    public ResponseEntity<CommandResponse> executeCommand(@RequestBody CommandRequest request) {
        CommandResponse response = commandParserService.parseCommand(request.getQuery());
        return ResponseEntity.ok(response);
    }
}
