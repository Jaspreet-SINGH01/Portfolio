package com.videoflix.users_microservice.services.async;

import com.videoflix.users_microservice.services.EmailService;
import com.videoflix.users_microservice.services.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceAsync {

    private static EmailService emailService;
    private final SmsService smsService;
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceAsync.class);

    public NotificationServiceAsync(SmsService smsService) {
        this.smsService = smsService;
    }

    @Async("notificationExecutor")
    public static void sendWelcomeEmailAsync(String to, String username) {
        try {
            emailService.sendWelcomeEmail(to, username);
            logger.info("Email de bienvenue envoyé à : {}", to);
        } catch (Exception e) {
            logger.error("Échec de l'envoi de l'email de bienvenue à : {}", to, e);
        }
    }

    @Async("notificationExecutor")
    public void sendVerificationCodeAsync(String to, String code) {
        try {
            smsService.sendVerificationCode(to, code);
            logger.info("Code de vérification envoyé par SMS à : {}", to);
        } catch (Exception e) {
            logger.error("Échec de l'envoi du code de vérification par SMS à : {}", to, e);
        }
    }
}