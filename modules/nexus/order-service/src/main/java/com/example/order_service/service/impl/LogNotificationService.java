package com.example.order_service.service.impl;

import com.example.order_service.dto.request.SendNotificationRequestDto;
import com.example.order_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogNotificationService implements NotificationService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogNotificationService.class);

    @Override
    public void sendNotification(SendNotificationRequestDto request) {
        log.info("Sending notification: {}", request);
    }
}
