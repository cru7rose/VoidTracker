package com.example.planning_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.planning_service.entity.CommunicationLogEntity;
import com.example.planning_service.repository.CommunicationLogRepository;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final CommunicationLogRepository logRepository;

    public void sendMagicLink(String email, String token) {
        String link = "http://localhost:5173/driver/login?token=" + token;

        log.info("==================================================================");
        log.info("SENDING MAGIC LINK TO: {}", email);
        log.info("LINK: {}", link);
        log.info("==================================================================");

        CommunicationLogEntity logEntity = new CommunicationLogEntity();
        logEntity.setTimestamp(java.time.Instant.now());
        logEntity.setChannel("EMAIL"); // Was setType
        // logEntity.setDirection("OUTBOUND"); // Field removed
        logEntity.setRecipient(email); // Was setContact
        logEntity.setMessageContent("Magic Link sent to " + email); // Was setContent

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your Driver Access Link");
            message.setText("Hello,\n\nHere is your magic link to access the Driver App:\n\n" + link
                    + "\n\nThis link will expire in 15 minutes.\n\nSafe travels!");

            mailSender.send(message);
            log.info("Email sent successfully to {}", email);

            logEntity.setStatus("DELIVERED");
        } catch (Exception e) {
            log.error("Failed to send email to {}", email, e);
            logEntity.setStatus("FAILED");
            logEntity.setMessageContent(logEntity.getMessageContent() + ". Error: " + e.getMessage());
        } finally {
            logRepository.save(logEntity);
        }
    }
}
