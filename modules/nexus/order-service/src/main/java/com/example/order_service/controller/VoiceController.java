package com.example.order_service.controller;

import com.example.order_service.service.VoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
@Tag(name = "Voice Interface", description = "API for voice commands and transcription")
public class VoiceController {

    private final VoiceService voiceService;

    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Transcribe audio file to text")
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(voiceService.transcribe(file));
    }
}
