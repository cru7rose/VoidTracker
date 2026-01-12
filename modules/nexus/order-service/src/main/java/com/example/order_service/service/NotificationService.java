package com.example.order_service.service;

import com.example.order_service.dto.request.SendNotificationRequestDto;

public interface NotificationService {
    void sendNotification(SendNotificationRequestDto request);
}
