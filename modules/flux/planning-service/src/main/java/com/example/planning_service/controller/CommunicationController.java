package com.example.planning_service.controller;

import com.example.planning_service.entity.CommunicationLogEntity;
import com.example.planning_service.repository.CommunicationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/communications")
@RequiredArgsConstructor
public class CommunicationController {

    private final CommunicationLogRepository logRepository;

    @GetMapping
    public ResponseEntity<List<CommunicationLogEntity>> getAllLogs() {
        return ResponseEntity.ok(logRepository.findAllByOrderByTimestampDesc());
    }
}
