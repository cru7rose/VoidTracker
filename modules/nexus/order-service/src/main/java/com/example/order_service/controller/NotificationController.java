package com.example.order_service.controller;

import com.example.order_service.dto.request.SendNotificationRequestDto;
import com.example.order_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "API for sending push notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "Send a push notification to a user")
    @ApiResponse(responseCode = "200", description = "Notification sent successfully")
    public ResponseEntity<Void> sendNotification(@Valid @RequestBody SendNotificationRequestDto request) {
        notificationService.sendNotification(request);
        return ResponseEntity.ok().build();
    }
}
