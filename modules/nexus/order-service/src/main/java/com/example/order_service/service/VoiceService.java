package com.example.order_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Service
public class VoiceService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VoiceService.class);

    public String transcribe(MultipartFile audioFile) {
        log.info("Received audio file for transcription: {}, size: {}", audioFile.getOriginalFilename(),
                audioFile.getSize());

        // Mock Transcription Logic
        // In a real implementation, this would call OpenAI Whisper API

        try {
            // Simulate processing delay
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "Driver reported an issue at " + Instant.now().toString() + ". Please investigate.";
    }
}
