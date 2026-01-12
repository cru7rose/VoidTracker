package com.example.planning_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@danxils.com}")
    private String fromEmail;

    public void sendMagicLink(String toEmail, String token) {
        try {
            String link = "https://dashboard.danxils.com/driver/login?token=" + token;
            String subject = "Your Danxils Driver Access Link";
            String body = String.format("""
                    Hello Driver,

                    Here is your access link for today's route:

                    %s

                    Or enter this token manually: %s

                    This link expires in 24 hours.

                    Safe driving!
                    Danxils Logistics
                    """, link, token);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Sent magic link email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}", toEmail, e);
            // In a real system, we might fallback to n8n or SMS here
        }
    }
}
