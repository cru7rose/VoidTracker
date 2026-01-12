package com.example.iam.support.email;

import com.example.iam.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogEmailService implements EmailService {
    @Override
    public void sendRegistrationEmail(String to, String token) {
        String registrationLink = "http://localhost:5173/register?token=" + token; // Przykładowy link do frontendu
        log.info("---- EMAIL SIMULATION ----");
        log.info("To: {}", to);
        log.info("Subject: Zaproszenie do systemu Voidtracker");
        log.info("Body: Aby dokończyć rejestrację, kliknij w link: {}", registrationLink);
        log.info("--------------------------");
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = "http://localhost:81/reset-password?token=" + token; // Link to Admin Frontend
        log.info("---- EMAIL SIMULATION (RESET PASSWORD) ----");
        log.info("To: {}", to);
        log.info("Subject: Reset hasła w systemie Voidtracker");
        log.info("Body: Aby zresetować hasło, kliknij w link: {}", resetLink);
        log.info("--------------------------");
    }
}