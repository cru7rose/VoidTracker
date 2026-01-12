package com.example.audit_service.consumer;

import com.example.audit_service.service.AuditService;
import com.example.danxils_commons.event.UserCreatedEvent;
import com.example.danxils_commons.event.UserDeletedEvent;
import com.example.danxils_commons.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * ARCHITEKTURA: Nowy konsument Kafki, dedykowany do nasłuchiwania na
 * zdarzenia pochodzące z domeny użytkowników (IAM). Jego zadaniem jest
 * przechwytywanie informacji o tworzeniu, modyfikacji i usuwaniu
 * użytkowników i przekazywanie ich do zapisu w serwisie audytu.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventsConsumer {

    private final AuditService auditService;

    private static final String ENTITY_TYPE_USER = "USER";
    private static final String SERVICE_NAME_IAM = "iam-service";

    @KafkaListener(topics = "#{'${app.kafka.topics.users-created}'}", groupId = "audit-service-group")
    public void handleUserCreated(@Payload UserCreatedEvent event) {
        log.debug("Consumed UserCreatedEvent for userId: {}", event.getUserId());
        auditService.recordEvent(
                "USER_CREATED",
                ENTITY_TYPE_USER,
                event.getUserId().toString(),
                event.getPerformedBy(),
                SERVICE_NAME_IAM,
                event
        );
    }

    @KafkaListener(topics = "#{'${app.kafka.topics.users-updated}'}", groupId = "audit-service-group")
    public void handleUserUpdated(@Payload UserUpdatedEvent event) {
        log.debug("Consumed UserUpdatedEvent for userId: {}", event.getUserId());
        auditService.recordEvent(
                "USER_UPDATED",
                ENTITY_TYPE_USER,
                event.getUserId().toString(),
                event.getPerformedBy(),
                SERVICE_NAME_IAM,
                event
        );
    }

    @KafkaListener(topics = "#{'${app.kafka.topics.users-deleted}'}", groupId = "audit-service-group")
    public void handleUserDeleted(@Payload UserDeletedEvent event) {
        log.debug("Consumed UserDeletedEvent for userId: {}", event.getUserId());
        auditService.recordEvent(
                "USER_DELETED",
                ENTITY_TYPE_USER,
                event.getUserId().toString(),
                event.getPerformedBy(),
                SERVICE_NAME_IAM,
                event
        );
    }
}