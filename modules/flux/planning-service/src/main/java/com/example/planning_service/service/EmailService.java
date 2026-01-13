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

    /**
     * Send magic link email to driver
     * 
     * @param toEmail Driver's email address
     * @param magicLinkUrl Full magic link URL (e.g., https://driver.voidtracker.app/auth?token=xxx)
     * @param driverName Optional driver name for personalization
     */
    public void sendMagicLink(String toEmail, String magicLinkUrl, String driverName) {
        try {
            String subject = "Twoja trasa na dzisiaj - VoidTracker";
            String greeting = driverName != null && !driverName.isBlank() 
                    ? "Witaj " + driverName + "!" 
                    : "Witaj!";
            
            String body = String.format("""
                    %s
                    
                    Twoja trasa została przypisana i jest gotowa do wyświetlenia.
                    
                    Kliknij poniższy link aby zobaczyć trasę:
                    %s
                    
                    Link wygasa za 24 godziny.
                    
                    Bezpiecznej jazdy!
                    Zespół VoidTracker
                    """, greeting, magicLinkUrl);

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
            throw new RuntimeException("Failed to send magic link email", e);
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    public void sendMagicLink(String toEmail, String token) {
        String magicLinkUrl = "https://driver.voidtracker.app/auth?token=" + token;
        sendMagicLink(toEmail, magicLinkUrl, null);
    }
}
